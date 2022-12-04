package com.zhaoyanpeng.subzlib.strategy;

import com.zhaoyanpeng.subzlib.context.GlobalContext;
import com.zhaoyanpeng.subzlib.entity.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;


@Slf4j
public class RemainBookByLanguageStrategy implements IShouldBookNeedToBeDelete, Ordered {

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public boolean needToBeDelete(Book book) {
        if (GlobalContext.getInstance().getRemainLanguages().contains(book.getLanguage())) {
            log.warn("要保留的语言{}不存在!", book.getLanguage());
        }
        // 包含在需要保留的language中，needToBeDelete = false
        return !GlobalContext.getInstance().getRemainLanguages().contains(book.getLanguage());
    }
}
