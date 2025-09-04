package com.wenjia.follow;


import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wenjia.follow.mapper")
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.wenjia.follow.service")
public class FollowApplication {
    public static void main(String[] args) {
        SpringApplication.run(FollowApplication.class, args);
    }
}