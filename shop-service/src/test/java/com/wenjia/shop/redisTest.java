package com.wenjia.shop;

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

    RedisTemplate<String,Object> redisTemplate;
    @Autowired
    RedisConnectionFactory connectionFactory;

    @BeforeEach
    public void bef(){
        redisTemplate= new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

    //todo 看看我写的这个测试类能不能正常运行吧
    @Test
    public void teeet(){
        redisTemplate.opsForValue().set("1233123",null);
    }

}
