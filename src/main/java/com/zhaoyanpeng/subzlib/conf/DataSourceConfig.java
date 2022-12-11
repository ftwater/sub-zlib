package com.zhaoyanpeng.subzlib.conf;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DataSourceConfig
 *
 * @author zhaoyanpeng
 * @date 2022/12/9 16:58
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        CloseAfterSchedulerHikariDataSource dataSource = null;
        try {
            dataSource = new CloseAfterSchedulerHikariDataSource();
            dataSource.setJdbcUrl(properties.getUrl());
            dataSource.setDriverClassName(properties.getDriverClassName());
            dataSource.setUsername(properties.getUsername());
            dataSource.setPassword(properties.getPassword());
            dataSource.setConnectionTestQuery("SELECT 1");
            dataSource.setLoginTimeout(3000);
            return dataSource;

        } catch (Exception e) {
            if (dataSource != null) {
                dataSource.close();
            }
        }
        return dataSource;
    }
}
