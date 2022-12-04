package com.zhaoyanpeng.subzlib.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoyanpeng.subzlib.conf.GlobalContext;
import com.zhaoyanpeng.subzlib.entity.Book;
import com.zhaoyanpeng.subzlib.exception.DeleteBookFailException;
import com.zhaoyanpeng.subzlib.exception.DeleteJudgeException;
import com.zhaoyanpeng.subzlib.mapper.BookMapper;
import com.zhaoyanpeng.subzlib.model.OptimizCountModel;
import com.zhaoyanpeng.subzlib.service.IBookService;
import com.zhaoyanpeng.subzlib.optimize.IShouldBookNeedToBeDelete;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaoyanpeng
 * @since 2022-12-02
 */
@Service
@Slf4j
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements IBookService {
    private final List<IShouldBookNeedToBeDelete> deleteJudges = new ArrayList<>();

    private final BookMapper bookMapper;

    @PostConstruct
    public void init() {
        Set<String> deleteJudgeNames = GlobalContext.getInstance().getDeleteJudges();
        for (String deleteJudgeName : deleteJudgeNames) {
            try {
                Class<?> clazz = Class.forName(deleteJudgeName);
                IShouldBookNeedToBeDelete deleteJudge = (IShouldBookNeedToBeDelete) clazz.newInstance();
                deleteJudges.add(deleteJudge);
            } catch (Exception e) {
                log.error("实例化deleteJudege实现失败", e);
                throw new DeleteJudgeException("实例化deleteJudege实现失败:" + deleteJudgeName);
            }

        }
        if (deleteJudges.isEmpty()) {
            throw new DeleteJudgeException("请配置sub-zlib.deleteJudges配置项目");
        }
    }

    public BookServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Async("bookOptimizePool")
    @Override
    public void deleteBook(String baseDir, Book book, Map<String, OptimizCountModel> optimizedMap)
            throws IOException {
        if (needToBeDelete(book)) {
            String filePath =
                    baseDir + File.separator +
                            book.getPilimiTorrent().replace(".torrent", "") + File.separator +
                            book.getZlibraryId();
            try {
                Files.delete(Paths.get(filePath));
            } catch (NoSuchFileException e1) {
                log.info("{} 文件不存在", filePath);
            } catch (DirectoryNotEmptyException e2) {
                log.error("{} 目录不为空", filePath);
                throw new DeleteBookFailException(e2);
            }
            optimizedMap.get(book.getLanguage()).addOptimizedZlibraryIdsForSave(book.getZlibraryId());
            log.trace("{} 删除成功", book.getZlibraryId());
        }
    }

    @Override
    public List<OptimizCountModel> getOptimizeCountModel() {
        return bookMapper.getOptimizeCountModels();
    }

    @Override
    public void saveBookOptimizeLog(List<Integer> zlibraryIds) {
        if (CollectionUtils.isEmpty(zlibraryIds)) {
            return;
        }
        bookMapper.batchInsertBookOptimizeLog(zlibraryIds);
    }


    private boolean needToBeDelete(Book book) {
        boolean needToBeDelete = false;
        for (IShouldBookNeedToBeDelete deleteJudge : deleteJudges) {
            needToBeDelete = deleteJudge.needToBeDelete(book);
            // 只要有一个策略不允许删除，就不能删除
            if (!needToBeDelete) {
                return false;
            }
        }
        return needToBeDelete;
    }
}
