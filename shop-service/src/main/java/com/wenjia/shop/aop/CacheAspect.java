package com.wenjia.shop.aop;

import com.wenjia.common.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final RedisTemplate<String,Object> redisTemplate;
    @Pointcut("execution(* com.wenjia.shop.service.ShopServiceImpl.*(..))")
    public void basePackage() {}

    /**
     * 删除商铺的分页缓存
     */
    @After("basePackage()&&@annotation(com.wenjia.shop.annotation.PageCacheEvict)")
    public void pageCacheEvict(JoinPoint joinPoint){
        String keyPattern="shopPage:*";
        int batchSize=1000;
        List<String> KeysToDelete=new ArrayList<>(batchSize);
        //获取游标
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions()
                .match(keyPattern)
                .count(100)
                .build());
        //游标不断向后走
        while(cursor.hasNext()){
            String key=cursor.next();
            KeysToDelete.add(key);
            //如果达到了容量，就进行删除
            if(KeysToDelete.size()>=batchSize){
                redisTemplate.delete(KeysToDelete);
                KeysToDelete.clear();
            }
        }
        //删除集合中还剩余的key
        if(!KeysToDelete.isEmpty()){
            redisTemplate.delete(KeysToDelete);
        }
    }

    /**
     * 删除单个商铺的缓存
     */
    @After("basePackage()&&@annotation(com.wenjia.shop.annotation.ShopCacheEvict)")
    public void shopCacheEvict(JoinPoint joinPoint){
        Long id=(Long)joinPoint.getArgs()[0];
        redisTemplate.delete(RedisConstant.SHOP_KEY+id);
    }
}
