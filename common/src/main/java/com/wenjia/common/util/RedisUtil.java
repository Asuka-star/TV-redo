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

    public static Long incrBy(RedisTemplate<String,Object> redisTemplate, List<String> keys, Object... params){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/incrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,keys,params);
    }

    public static Long decrBy(RedisTemplate<String,Object> redisTemplate,List<String> keys,Object... params){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/decrBy.lua")));
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript,keys,params);
    }

    public static boolean  actionLimit(RedisTemplate<String,Object> redisTemplate,String key,int maxCount,int windowSec){
        long now = System.currentTimeMillis() / 1000;
        String member = UUID.randomUUID().toString();
        DefaultRedisScript<String> redisScript=new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/actionLimit.lua")));
        redisScript.setResultType(String.class);
        String execute = redisTemplate.execute(redisScript, Collections.singletonList(key),maxCount,windowSec,now,member);
        return "1".equals(execute);
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