package com.wenjia.thumb.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.ThumbDTO;
import com.wenjia.api.domain.pageQuery.ThumbPageQuery;
import com.wenjia.api.domain.po.Thumb;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.domain.vo.ThumbVO;
import com.wenjia.api.service.ThumbService;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.ThumbException;
import com.wenjia.common.util.RedisUtil;
import com.wenjia.thumb.mapper.ThumbMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper,Thumb> implements ThumbService {

    private final RedissonClient redissonClient;
    private final RedisTemplate<String,Object> redisTemplate;

    @Override
    public void thumb(ThumbDTO thumbDTO) {
        //检查数据
        Integer type = thumbDTO.getType();
        String content = thumbDTO.getContent();
        if(content.length()>20){
            content=content.substring(0,17)+"...";
        }
        //判断是否点过赞
        Long targetId = thumbDTO.getTargetId();
        Long userId = BaseContext.getCurrentId();
        if(hasThumb(type,userId, targetId)){
            throw new ThumbException("您已经点过赞了");
        }
        String lockKey= RedisConstant.LOCK_THUMB_KEY+type+":"+userId+":"+targetId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L, TimeUnit.SECONDS)){
                try {
                    //修改缓存中点赞记录和点赞数
                    String hasThumbKey= RedisConstant.THUMB_KEY+type+":"+userId+":"+ targetId;
                    String thumbCountKey=RedisConstant.THUMB_COUNT_KEY+type+":"+ targetId;
                    redisTemplate.opsForValue().set(hasThumbKey,1,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);

                    //执行lua脚本
                    List<String> keys=new ArrayList<>();
                    keys.add(thumbCountKey);
                    RedisUtil.incrBy(redisTemplate,keys,1,String.valueOf(TimeUnit.MINUTES.toSeconds(RedisConstant.EXPIRE_TIME)));

                    //数据库中的点赞表新增数据
                    Thumb thumb=BeanUtil.copyProperties(thumbDTO,Thumb.class);
                    thumb.setCreateTime(LocalDateTime.now());
                    //todo 需要开启事务进行更新商铺，评论，帖子的点赞数
                    save(thumb);
                    if(type==0) redisTemplate.delete(RedisConstant.SHOP_KEY+thumb.getTargetId());
                    if(type==2) redisTemplate.opsForHash().increment(RedisConstant.POST_KEY + thumb.getTargetId(),"thumbNumber",1L);
                } finally {
                    lock.unlock();
                }
            }else{
                //这里是根据用户的信息来进行加锁的，所以获取不到锁说明用户重复操作
                throw new ThumbException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelThumb(ThumbDTO thumbDTO) {
        //检查数据
        Integer type = thumbDTO.getType();
        if(!(type==0||type==1||type==2)) throw new ThumbException("当前点赞类型错误");
        //判断是否点过赞
        Long targetId = thumbDTO.getTargetId();
        Long userId = BaseContext.getCurrentId();
        if(!hasThumb(thumbDTO.getType(),userId, targetId)){
            throw new ThumbException("您并没有点过这个赞");
        }
        String lockKey= RedisConstant.LOCK_THUMB_KEY+type+":"+userId+":"+targetId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L,TimeUnit.SECONDS)){
                try {
                    //修改缓存中点赞记录和点赞数
                    String hasThumbKey= RedisConstant.THUMB_KEY+type+":"+userId+":"+ targetId;
                    String thumbCountKey=RedisConstant.THUMB_COUNT_KEY+type+":"+ targetId;
                    redisTemplate.opsForValue().set(hasThumbKey,0,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);

                    //执行lua脚本
                    List<String> keys=new ArrayList<>();
                    keys.add(thumbCountKey);
                    RedisUtil.decrBy(redisTemplate,keys,1,String.valueOf(TimeUnit.MINUTES.toSeconds(RedisConstant.EXPIRE_TIME)));


                    //数据库中的点赞表删除数据
                    Thumb thumb = Thumb.builder()
                            .userId(userId)
                            .type(type)
                            .targetId(targetId)
                            .build();
                    lambdaUpdate().eq(Thumb::getType,type).eq(Thumb::getUserId,userId).eq(Thumb::getTargetId,targetId).remove();
                    if(type==0) redisTemplate.delete(RedisConstant.SHOP_KEY+thumb.getTargetId());
                    if(type==2) redisTemplate.opsForHash().increment(RedisConstant.POST_KEY+thumb.getTargetId(),"thumbNumber",-1L);
                } finally {
                    lock.unlock();
                }
            }else{
                throw new ThumbException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean hasThumb(Integer type, Long userId, Long targetId) {
        String hasThumbKey= RedisConstant.THUMB_KEY+type+":"+userId+":"+ targetId;
        String value = Objects.requireNonNull(redisTemplate.opsForValue().get(hasThumbKey)).toString();
        if(value!=null) return "1".equals(value);
        //缓存未命中，查询数据库
        Long count = lambdaQuery().eq(Thumb::getType, type).eq(Thumb::getUserId, userId).eq(Thumb::getTargetId, targetId).count();
        boolean hasThumb=count>0;
        //存入缓存中
        redisTemplate.opsForValue().set(hasThumbKey,hasThumb?1:0,RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
        return hasThumb;
    }

    @Override
    public PageResult<ThumbVO> pageQuery(ThumbPageQuery thumbPageQuery) {
        //查询当前页的数据
        Page<Thumb> page = lambdaQuery().eq(Thumb::getType, thumbPageQuery.getType())
                .eq(Thumb::getUserId, BaseContext.getCurrentId())
                .page(thumbPageQuery.toMpPage("id", true));
        //将thumb转成thumbVO
        List<ThumbVO> thumbVOList = page.getRecords().stream()
                .map(thumb -> BeanUtil.copyProperties(thumb, ThumbVO.class))
                .collect(Collectors.toList());
        //封装成PageResult类
        PageResult<ThumbVO> pageResult = new PageResult<>();
        pageResult.setRecords(thumbVOList);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }
}
