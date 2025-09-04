package com.wenjia.shop;

import com.wenjia.api.domain.vo.ShopVO;
import com.wenjia.common.constant.RedisConstant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalTime;


@SpringBootTest
public class redisTest {

    //common模块里面已经配置好了的
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Test
    public void teeet(){
        ShopVO shopVO = new ShopVO();
        shopVO.setAddress("sadf");
        redisTemplate.opsForValue().set("1",shopVO);
        redisTemplate.opsForValue().set("2","2");
        redisTemplate.setStringSerializer(new StringRedisSerializer());
        redisTemplate.opsForValue().set("3",shopVO);
        redisTemplate.opsForValue().set("4","4");
        stringRedisTemplate.opsForValue().set("5","5");

    }

}
