package com.wenjia.thumb;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wenjia.thumb.mapper")
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.wenjia.thumb.service")
public class ThumbApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThumbApplication.class, args);
    }
}