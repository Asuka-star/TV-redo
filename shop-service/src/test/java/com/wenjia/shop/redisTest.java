package com.wenjia.shop;

import com.wenjia.common.constant.RedisConstant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@SpringBootTest
public class redisTest {

    //common模块里面已经配置好了的
    @Autowired
    RedisTemplate<String,Object> redisTemplate;


    @Test
    public void teeet(){
        Object o = redisTemplate.opsForValue().get(RedisConstant.COUPON_STOCK_KEY + "1945384382848655361");
        if(o instanceof Integer){
            System.out.println(123);
        }
        System.out.println(o);
    }

}
