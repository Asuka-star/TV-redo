package com.wenjia.favorite;


import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.wenjia.favorite.mapper")
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.wenjia.favorite.service")
public class FavoriteApplication {
    public static void main(String[] args) {
        SpringApplication.run(FavoriteApplication.class, args);
    }
}