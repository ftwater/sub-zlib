package com.zhaoyanpeng.subzlib.optimize;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoyanpeng.subzlib.conf.GlobalContext;
import com.zhaoyanpeng.subzlib.entity.Book;
import com.zhaoyanpeng.subzlib.exception.SubZlibError;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;
import com.zhaoyanpeng.subzlib.service.IBookService;
import lombok.extern.slf4j.Slf4j;
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
public class BookOptimizeScheduler implements ApplicationRunner {

    private final BookOptimizer bookOptimizer;

    private final IBookService bookService;

    private final File baseDir;

    private volatile boolean optimizing = false;

    private final Map<String, OptimizCountModel> optimizMap = new HashMap<>();

    private final Timer timer = new Timer();

    private static final long ONE_HOUR = 1000L * 60 * 60;
    private static final long TEN_SECONDS = 1000L * 10;


    public BookOptimizeScheduler(BookOptimizer bookOptimizer, IBookService bookService) {
        this.bookOptimizer = bookOptimizer;
        this.bookService = bookService;
        baseDir = new File(GlobalContext.getInstance().getBasePath());
        if (!baseDir.exists()) {
            throw new SubZlibError("basePath" + GlobalContext.getInstance().getBasePath() + " 未指定或不存在");
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        Timer scheduler = new Timer();
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
        }, 10000L, ONE_HOUR);
    }

    private void doOptimize() throws InterruptedException {
        log.info("优化开始");
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
        CountDownLatch countDownLatch = new CountDownLatch(languages.size());
        for (String language : languages) {
            try {
                bookOptimizer.optimizeBookByLanguage(countDownLatch, optimizMap, baseDir.getCanonicalPath(), language);
            } catch (IOException e) {
                log.error("language={}，删除书籍异常", language);
            }
        }
        countDownLatch.await();
        optimizing = false;
        stopMonitor();
        log.info("优化结束");
    }

    private void startMonitor() {
        TimerTask timerTask = new TimerTask() {
            final long allBookCount = optimizMap.values().stream()
                    .mapToLong(OptimizCountModel::getLanguageCount).sum();

            @Override
            public void run() {
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
                log.info("总体共{}条记录，已优化{}条记录，优化进度：{}%", allBookCount,
                        processedCount,
                        percent.floatValue() * 100);
                // 保存优化记录，优化结束 或者 每500条记录保存一次
                optimizMap.values().forEach(optimizCountModel -> {
                    if (optimizCountModel.isOptimizeFinished() &&
                            optimizCountModel.getOptimizedZlibraryIds().isEmpty()) {
                        return;
                    }
                    if (optimizCountModel.isOptimizeFinished() ||
                            optimizCountModel.getOptimizedZlibraryIds().size() >= 500) {
                        List<Integer> zlibraryIdsForSave = optimizCountModel.clearOptimizedZlibraryIdsForSave();
                        bookService.saveBookOptimizeLog(zlibraryIdsForSave);
                    }
                });
            }
        };
        timer.schedule(timerTask, 1000L, TEN_SECONDS);
    }

    private void stopMonitor() {
        timer.cancel();
    }

}
