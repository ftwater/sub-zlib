package com.zhaoyanpeng.subzlib.conf;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 一些全局变量
 *
 * @author zhaoyanpeng
 * @date 2022/12/2 14:14
 */
@Component
@Data
@ConfigurationProperties(prefix = "sub-zlib")
public class GlobalContext implements ApplicationContextAware, InitializingBean {

    private static GlobalContext globalContext;

    private ApplicationContext applicationContext;

    private String basePath;

    private Set<String> remainLanguages;

    private Set<String> deleteJudges;


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        globalContext = this;
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }
}
