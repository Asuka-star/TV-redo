package com.wenjia.coupon.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class RedisId {
    //定义一个开始时间戳，可以延长使用时间
    private final long BEGIN_TIMESTAMP = 1744758983L;
    //设定过期时间为一个月
    private final String EXPIRE_TIME = "2592000";
    //维护上一次更新的时间
    private  final AtomicLong lastTimestamp = new AtomicLong(0L);
    private final StringRedisTemplate redisTemplate;
    public Long createId(String keyPrefix) {
        //生成时间戳
        long nowTimestamp = System.currentTimeMillis()/1000;
        long timestamp = nowTimestamp - BEGIN_TIMESTAMP;
        if (lastTimestamp.get() > timestamp) {
            //时间回退了
            throw new RuntimeException("Redis生成id错误");
        }
        //当前日期
        String dateTime = LocalDateTime.now(ZoneOffset.ofHours(8)).format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //通过redis获取序列号
        long incr = 0L;
        //通过lua脚本进行incr和过期时间的原子操作
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/redisId.lua")));
        redisScript.setResultType(Long.class);
        incr=redisTemplate.execute(redisScript, Collections.singletonList("incr:" + keyPrefix + dateTime), EXPIRE_TIME);
        //更新上次更新的时间
        while (true) {
            long currLast = lastTimestamp.get();
            //如果其他线程进行了更新导致currLast大于timestamp，会使得lastTimestamp会往回退
            if (timestamp > currLast) {
                if (lastTimestamp.compareAndSet(currLast, timestamp)) {
                    break;
                }
            } else {
                break;
            }
        }
        return timestamp << 32 | incr;
    }
}
