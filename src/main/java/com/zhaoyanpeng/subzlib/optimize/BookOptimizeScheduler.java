package com.zhaoyanpeng.subzlib.optimize;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoyanpeng.subzlib.conf.GlobalContext;
import com.zhaoyanpeng.subzlib.entity.Book;
import com.zhaoyanpeng.subzlib.exception.BookOptimizeError;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;
import com.zhaoyanpeng.subzlib.service.IBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 调度
 *
 * @author zhaoyanpeng
 * @date 2022/12/2 13:12
 */
@Component
@Slf4j
public class BookOptimizeScheduler implements ApplicationRunner, DisposableBean {

    private final BookOptimizer bookOptimizer;

    private final IBookService bookService;

    private final File baseDir;

    private volatile boolean optimizing = false;

    private final Map<String, OptimizCountModel> optimizMap = new HashMap<>();

    private final Timer monitorTimer = new Timer();

    private final Timer scheduler = new Timer();

    private static final long ONE_HOUR = 1000L * 60 * 60;
    private static final long ONE_SECONDS = 1000L;
    private static final long TEN_SECONDS = 10000L;


    public BookOptimizeScheduler(BookOptimizer bookOptimizer, IBookService bookService) {
        this.bookOptimizer = bookOptimizer;
        this.bookService = bookService;
        baseDir = new File(GlobalContext.getInstance().getBasePath());
        if (!baseDir.exists()) {
            throw new BookOptimizeError("basePath" + GlobalContext.getInstance().getBasePath() + " 未指定或不存在");
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    doOptimize();
                } catch (InterruptedException e) {
                    log.error("调度优化器定时启动失败", e);
                    Thread.currentThread().interrupt();
                }
            }
        }, TEN_SECONDS, ONE_HOUR);
    }

    private void doOptimize() throws InterruptedException {
        log.info("优化开始");
        int tryTimes = 0;
        // 重试3次，如果有任务在执行就不再执行
        while (tryTimes < 3 && optimizing) {
            Thread.sleep(ONE_SECONDS);
            tryTimes++;
        }
        if (optimizing) {
            log.info("当前存在优化任务，不需要执行。");
            return;
        }
        optimizing = true;
        List<OptimizCountModel> countModels = bookService.getOptimizeCountModel();
        countModels.forEach(optimizCountModel -> optimizMap.put(optimizCountModel.getLanguage(), optimizCountModel));
        startMonitor();
        Set<String> languages =
                bookService.list(new QueryWrapper<Book>().select("distinct language")).stream().map(Book::getLanguage)
                        .collect(Collectors.toSet());
        for (String language : languages) {
            try {
                bookOptimizer.optimizeBookByLanguage(optimizMap, baseDir.getCanonicalPath(), language);
            } catch (IOException e) {
                log.error("language={}，删除书籍异常", language);
            }
        }
        log.info("优化结束");
    }

    private void startMonitor() {
        TimerTask timerTask = new TimerTask() {
            final long allBookCount = optimizMap.values().stream()
                    .mapToLong(OptimizCountModel::getLanguageCount).sum();
            int execTimes = 0;

            @Override
            public void run() {
                if (!optimizing) {
                    monitorTimer.cancel();
                    return;
                }
                execTimes++;
                optimizMap.forEach(
                        (language, optimizCountModel) -> {
                            long waitToProcesseCount =
                                    optimizCountModel.getLanguageCount() - optimizCountModel.getProcessedCount().get();
                            if (waitToProcesseCount > 0) {
                                log.trace("{}已处理{},还有{}待处理", language,
                                        optimizCountModel.getProcessedCount(),
                                        waitToProcesseCount
                                );
                            } else {
                                optimizCountModel.setOptimizeFinished(true);
                            }
                        });
                long processedCount = optimizMap.values().stream()
                        .map(OptimizCountModel::getProcessedCount)
                        .mapToLong(AtomicLong::get).sum();
                BigDecimal percent = BigDecimal.valueOf(processedCount)
                        .divide(BigDecimal.valueOf(allBookCount), 8,
                                RoundingMode.CEILING);
                // 一分钟打印一次，或者全部优化结束打印出来
                if (execTimes % 60 == 0 || processedCount >= allBookCount) {
                    log.info("总体共{}条记录，已优化{}条记录，优化进度：{}%", allBookCount,
                            processedCount,
                            percent.floatValue() * 100);
                }
                // 保存优化记录，优化结束 或者 每500条记录保存一次
                saveBookOptmizeLog(false);
                // 所有语言都处理完了，变更状态
                if (optimizMap.values().stream().filter(OptimizCountModel::isOptimizeFinished).count() ==
                        optimizMap.size()) {
                    optimizing = false;
                }
            }
        };
        monitorTimer.schedule(timerTask, ONE_SECONDS, ONE_SECONDS);
    }

    private void saveBookOptmizeLog(boolean isDestroy) {
        optimizMap.values().forEach(optimizCountModel -> {
            if (isDestroy) {
                List<Integer> zlibraryIdsForSave = optimizCountModel.clearOptimizedZlibraryIdsForSave();
                bookService.saveBookOptimizeLog(zlibraryIdsForSave);
            } else {
                if (optimizCountModel.isOptimizeFinished() &&
                        optimizCountModel.getOptimizedZlibraryIds().isEmpty()) {
                    return;
                }
                if (optimizCountModel.isOptimizeFinished() ||
                        optimizCountModel.getOptimizedZlibraryIds().size() >= 500) {
                    List<Integer> zlibraryIdsForSave = optimizCountModel.clearOptimizedZlibraryIdsForSave();
                    bookService.saveBookOptimizeLog(zlibraryIdsForSave);
                }
            }
        });
    }

    @Override
    public void destroy() {
        scheduler.cancel();
        monitorTimer.cancel();
        saveBookOptmizeLog(true);
    }
}
