package com.wenjia.favorite.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.po.Favorite;
import com.wenjia.api.domain.vo.PostVO;
import com.wenjia.api.service.FavoriteService;
import com.wenjia.api.service.PostService;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.FavoriteException;
import com.wenjia.common.exception.ThumbException;
import com.wenjia.favorite.mapper.FavoriteMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@DubboService
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper,Favorite> implements FavoriteService {

    @DubboReference
    private PostService postService;

    private final RedisTemplate<String,Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    @GlobalTransactional
    public void favorite(Long postId) {
        //检查数据,没有的话postService中会报错
        PostVO postVO = postService.getById(postId);
        //判断是否收藏过
        Long userId = BaseContext.getCurrentId();
        if(hasFavorite(userId,postId)){
            throw new FavoriteException("您已经收藏过了");
        }
        String lockKey= RedisConstant.LOCK_FAVORITE_KEY+userId+":"+postId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L, TimeUnit.SECONDS)){
                try {
                    //修改缓存中收藏记录
                    String hasFavoriteKey= RedisConstant.FAVORITE_KEY+userId+":"+postId;
                    redisTemplate.opsForValue().set(hasFavoriteKey,1,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
                    //数据库中的收藏表新增数据
                    Favorite favorite = Favorite.builder().userId(userId)
                            .postId(postId).createTime(LocalDateTime.now()).build();
                    redisTemplate.opsForHash().increment(RedisConstant.POST_KEY+postId,"favoriteNumber",1L);
                    save(favorite);
                    postService.incrFavoriteNumber(postId);
                } finally {
                    lock.unlock();
                }
            }else{
                throw new FavoriteException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GlobalTransactional
    public void cancelFavorite(Long postId) {
        //检查数据,没有的话service中会报错
        PostVO postVO = postService.getById(postId);
        //判断是否收藏过
        Long userId = BaseContext.getCurrentId();
        if(!hasFavorite(userId,postId)){
            throw new ThumbException("您并没有收藏过");
        }
        String lockKey= RedisConstant.LOCK_FAVORITE_KEY+userId+":"+postId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L,TimeUnit.SECONDS)){
                try {
                    //修改缓存中收藏记录
                    String hasFavoriteKey= RedisConstant.FAVORITE_KEY+userId+":"+postId;
                    redisTemplate.opsForValue().set(hasFavoriteKey,0,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
                    //数据库中的收藏表删除数据
                    Favorite favorite = Favorite.builder().userId(userId)
                            .postId(postId).createTime(LocalDateTime.now()).build();
                    redisTemplate.opsForHash().increment(RedisConstant.POST_KEY+postId,"favoriteNumber",-1L);
                    lambdaUpdate().eq(Favorite::getUserId,userId).eq(Favorite::getPostId,postId).remove();
                    postService.decrFavoriteNumber(postId);
                } finally {
                    lock.unlock();
                }
            }else{
                throw new FavoriteException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasFavorite(Long userId, Long postId) {
        String hasFavoriteKey= RedisConstant.FAVORITE_KEY+userId+":"+postId;
        String value = (String) redisTemplate.opsForValue().get(hasFavoriteKey);
        if(value!=null) return "1".equals(value);
        //缓存未命中，查询数据库
        boolean hasFavorite=lambdaQuery()
                .eq(Favorite::getUserId,userId)
                .eq(Favorite::getPostId,postId).exists();
        //存入缓存中
        redisTemplate.opsForValue().set(hasFavoriteKey,hasFavorite?1:0,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
        return hasFavorite;
    }
}
