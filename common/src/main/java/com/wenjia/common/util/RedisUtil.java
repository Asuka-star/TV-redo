package com.wenjia.common.util;


import com.wenjia.common.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Long incrBy(String key,int value,long expiration){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/incrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,Collections.singletonList(key),value,expiration);
    }

    public Long decrBy(String key,int value,long expiration){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/decrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,Collections.singletonList(key),value,expiration);
    }

    public void rollBackStock(Long couponId,Long userId){
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/rollBackStock.lua")));
        redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_STOCK_KEY+couponId,RedisConstant.SECKILL_KEY+couponId),
                "1",userId.toString());
    }

    public Long updateStock(Long couponId,Integer stockChange){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/updateStock.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_KEY+couponId),stockChange.toString());
    }

    public Long checkSecKill(Long couponId,Long userId,Long expireTime){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/checkSecKill.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_STOCK_KEY+couponId,RedisConstant.SECKILL_KEY+couponId),
                userId.toString(),expireTime.toString());
    }

}