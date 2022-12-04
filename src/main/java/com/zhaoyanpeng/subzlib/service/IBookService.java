package com.zhaoyanpeng.subzlib.service;

import com.zhaoyanpeng.subzlib.entity.Book;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhaoyanpeng
 * @since 2022-12-02
 */
public interface IBookService extends IService<Book> {

    void deleteBook(String baseDir, Book book, Map<String, OptimizCountModel> optimizedZlibraryIds) throws IOException;

    List<OptimizCountModel> getOptimizCountModel();

    void saveBookOptimizLog(List<Integer> zlibraryIds);
}
