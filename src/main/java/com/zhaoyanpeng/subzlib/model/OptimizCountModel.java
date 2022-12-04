package com.zhaoyanpeng.subzlib.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 优化计数模型
 *
 * @author zhaoyanpeng
 * @date 2022/12/3 21:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizCountModel {
    private String language;
    private Long languageCount;
    private AtomicLong processedCount;
    private List<Integer> optimizedZlibraryIds = new ArrayList<>();
    private boolean optimizeFinished;

    private Lock logLock = new ReentrantLock();

    public List<Integer> clearOptimizedZlibraryIdsForSave() {
        logLock.lock();
        List<Integer> copyList = new ArrayList<>(optimizedZlibraryIds);
        optimizedZlibraryIds.clear();
        logLock.unlock();
        return copyList;
    }

    public void addOptimizedZlibraryIdsForSave(Integer optimizedZlibraryId) {
        logLock.lock();
        optimizedZlibraryIds.add(optimizedZlibraryId);
        logLock.unlock();
    }


}
