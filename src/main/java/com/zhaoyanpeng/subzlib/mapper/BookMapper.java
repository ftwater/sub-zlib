package com.zhaoyanpeng.subzlib.mapper;

import com.zhaoyanpeng.subzlib.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhaoyanpeng
 * @since 2022-12-02
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Select("select * from books t1 where language = #{language} and not exists(select * from book_optimize_log t2 " +
            "where t1.zlibrary_id = t2.zlibrary_id)")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(Book.class)
    void listBookByLanguage(@Param("language") String language, ResultHandler<Book> resultHandler);

    List<OptimizCountModel> getOptimizeCountModels();

    /**
     * 插入优化日志
     *
     * @param zlibraryIds zlibraryIds
     */
    void batchInsertBookOptimizeLog(@Param("zlibraryIds") List<Integer> zlibraryIds);
}
