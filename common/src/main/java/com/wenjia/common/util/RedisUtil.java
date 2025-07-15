package com.wenjia.common.util;


import com.wenjia.common.constant.RedisConstant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class RedisUtil {

    public static Long incrBy(RedisTemplate<String,Object> redisTemplate, String key,int value,long expiration){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/incrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,Collections.singletonList(key),value,expiration);
    }

    public static Long decrBy(RedisTemplate<String,Object> redisTemplate,String key,int value,long expiration){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/decrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,Collections.singletonList(key),value,expiration);
    }

    public static void rollBackStock(RedisTemplate<String,Object>redisTemplate,Long couponId,Long userId){
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/rollBackStock.lua")));
        redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_KEY+couponId,RedisConstant.SECKILL_KEY+couponId),
                "1",userId.toString());
    }

    public static Long updateStock(RedisTemplate<String,Object>redisTemplate,Long couponId,Integer stockChange){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/updateStock.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_KEY+couponId),stockChange.toString());
    }

    public static Long checkSecKill(RedisTemplate<String,Object>redisTemplate,Long couponId,Long userId,Long expireTime){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/checkSecKill.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,List.of(RedisConstant.COUPON_KEY+couponId,RedisConstant.SECKILL_KEY+couponId),
                userId.toString(),expireTime.toString());
    }

}