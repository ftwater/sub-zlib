package com.zhaoyanpeng.subzlib.optimize;

import com.zhaoyanpeng.subzlib.conf.GlobalContext;
import com.zhaoyanpeng.subzlib.entity.Book;
import com.zhaoyanpeng.subzlib.exception.DeleteJudgeConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.Set;


@Slf4j
public class RemainBookByLanguageStrategy implements IShouldBookNeedToBeDelete, Ordered {

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public boolean needToBeDelete(Book book) {
        Set<String> remainLanguages = GlobalContext.getInstance().getRemainLanguages();
        if (remainLanguages.isEmpty()) {
            throw new DeleteJudgeConfigException("RemainBookByLanguageStrategy没有获取到remainLanguages配置");
        }
        if (remainLanguages.contains(book.getLanguage())) {
            log.warn("要保留的语言{}不存在!", book.getLanguage());
        }
        // 包含在需要保留的language中，needToBeDelete = false
        return !remainLanguages.contains(book.getLanguage());
    }
}
