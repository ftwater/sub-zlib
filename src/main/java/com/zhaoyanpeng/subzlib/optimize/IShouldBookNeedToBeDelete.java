package com.zhaoyanpeng.subzlib.optimize;

import com.zhaoyanpeng.subzlib.entity.Book;

/**
 * @author zhaoyanpeng
 * @date 2022/12/2 15:21
 */
public interface IShouldBookNeedToBeDelete {
    boolean needToBeDelete(Book book);
}
