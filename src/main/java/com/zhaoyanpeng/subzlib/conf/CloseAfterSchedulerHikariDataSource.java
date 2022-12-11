package com.zhaoyanpeng.subzlib.conf;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * CloseAfterSchedulerHikariDataSource
 *
 * @author zhaoyanpeng
 * @date 2022/12/5 11:03
 */
@Component
@Slf4j
public class CloseAfterSchedulerHikariDataSource extends HikariDataSource {


    private static final int WAIT_TIME = 2000;

    private static final int MAX_WAIT = 60000;

    @Autowired
    private Executor languageProcessPool;
    @Autowired
    private Executor bookOptimizePool;


    @SneakyThrows
    @Override
    public void close() {
        if (bookOptimizePool != null) {
            ThreadPoolTaskExecutor bookOptimizeTaskExecutor = (ThreadPoolTaskExecutor) bookOptimizePool;
            bookOptimizeTaskExecutor.shutdown();
            int wait = 0;
            while (bookOptimizeTaskExecutor.getActiveCount() > 0 && wait < MAX_WAIT) {
                log.info("bookOptimizePool 还有{}个任务在执行...", bookOptimizeTaskExecutor.getActiveCount());
                Thread.sleep(WAIT_TIME);
                wait += WAIT_TIME;
            }
        }
        if (languageProcessPool != null) {
            ThreadPoolTaskExecutor languageTaskExecutor = (ThreadPoolTaskExecutor) languageProcessPool;
            languageTaskExecutor.shutdown();
            int wait = 0;
            while (languageTaskExecutor.getActiveCount() > 0 && wait < MAX_WAIT) {
                log.info("languageProcessPool 还有{}个任务在执行...", languageTaskExecutor.getActiveCount());
                Thread.sleep(WAIT_TIME);
                wait += WAIT_TIME;
            }
        }

        super.close();
    }
}
