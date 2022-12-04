package com.zhaoyanpeng.subzlib.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author zhaoyanpeng
 * @since 2022-12-02
 */
@TableName("books")
@Data
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Integer zlibraryId;

    private String dateAdded;

    private String dateModified;

    private String extension;

    private Long filesize;

    private Long filesizeReported;

    private String md5;

    private String md5Reported;

    private String title;

    private String author;

    private String publisher;

    private String language;

    private String series;

    private String volume;

    private String edition;

    private String year;

    private String pages;

    private String description;

    private String coverUrl;

    private Boolean inLibgen;

    private String pilimiTorrent;

    private Boolean unavailable;


}
