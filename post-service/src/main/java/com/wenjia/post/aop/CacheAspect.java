package com.wenjia.post.aop;

import com.wenjia.common.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final RedisTemplate<String,Object> redisTemplate;
    @Pointcut("execution(* com.wenjia.post.service.PostServiceImpl.*(..))")
    public void basePackage() {}

    /**
     * 删除单个动态的缓存
     */
    @After("basePackage()&&@annotation(com.wenjia.post.annotation.PostCacheEvict)")
    public void shopCacheEvict(JoinPoint joinPoint){
        Long id=(Long)joinPoint.getArgs()[0];
        redisTemplate.delete(RedisConstant.POST_KEY+id);
    }
}
