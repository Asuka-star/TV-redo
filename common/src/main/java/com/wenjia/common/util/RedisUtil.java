package com.wenjia.common.util;


import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
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
}