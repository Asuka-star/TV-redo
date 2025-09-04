package com.wenjia.thumb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class redisTest {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    //todo 看看我写的这个测试类能不能正常运行吧
    @Test
    public void teeet(){
        redisTemplate.opsForValue().set("1233123",123);
    }

}