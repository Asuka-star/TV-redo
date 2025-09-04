package com.wenjia.coupon;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@MapperScan("com.wenjia.coupon.mapper")
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.wenjia.coupon.service")
//启动类这里也要增加，因为springboot的自动配置的设置发生了改变
@Import(RocketMQAutoConfiguration.class)
public class CouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponApplication.class, args);
    }
}