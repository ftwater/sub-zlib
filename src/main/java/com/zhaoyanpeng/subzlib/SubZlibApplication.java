package com.zhaoyanpeng.subzlib;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhaoyanpeng.subzlib.mapper")
public class SubZlibApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubZlibApplication.class, args);
	}

}
