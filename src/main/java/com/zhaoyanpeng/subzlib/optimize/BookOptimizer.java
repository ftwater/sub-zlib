package com.zhaoyanpeng.subzlib.optimize;

import com.zhaoyanpeng.subzlib.entity.Book;
import com.zhaoyanpeng.subzlib.mapper.BookMapper;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;
import com.zhaoyanpeng.subzlib.service.IBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BookManager
 *
 * @author zhaoyanpeng
 * @date 2022/12/2 12:17
 */
@Component
@Slf4j
public class BookOptimizer {

    private final IBookService bookService;

    private final BookMapper bookMapper;


    public BookOptimizer(IBookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @Async("languageProcessPool")
    public void optimizeBookByLanguage(Map<String, OptimizCountModel> optimizMap,
                                       String baseDir, String language)
            throws IOException {
        log.info("{}开始优化", language);
        bookMapper.listBookByLanguage(language, resultContext -> {
            if (Thread.currentThread().isInterrupted()) {
                log.info("{}中断优化", language);
                resultContext.stop();
            }
            if (resultContext.getResultCount() == 0) {
                log.info("{}语言下不存在需要优化的书籍了", language);
                return;
            }
            Book book = resultContext.getResultObject();
            try {
                bookService.deleteBook(baseDir, book, optimizMap);
                incrementProcessedCount(language, optimizMap);
            } catch (IOException e) {
                log.error("bookId={},删除失败", book.getZlibraryId());
            }
        });
        log.info("{}优化结束", language);
    }

    private void incrementProcessedCount(String language, Map<String, OptimizCountModel> optimizMap) {
        OptimizCountModel countModel = optimizMap.getOrDefault(language, OptimizCountModel.builder()
                .language(language)
                .processedCount(new AtomicLong(0))
                .build());
        AtomicLong processedCount = countModel.getProcessedCount();
        processedCount.incrementAndGet();
        optimizMap.put(language, countModel);
    }

}
