package com.wenjia.follow.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.FollowDTO;
import com.wenjia.api.domain.pageQuery.FollowPageQuery;
import com.wenjia.api.domain.po.Follow;
import com.wenjia.api.domain.vo.FollowVO;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.service.FollowService;
import com.wenjia.api.service.ShopService;
import com.wenjia.api.service.UserService;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.FollowException;
import com.wenjia.common.util.RedisUtil;
import com.wenjia.follow.mapper.FollowMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@DubboService
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper,Follow> implements FollowService {

    @DubboReference
    private ShopService shopService;
    @DubboReference
    private UserService userService;

    //todo 消息发送者
    //private PostProducer postProducer;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String,Object> redisTemplate;

    @Override
    @GlobalTransactional
    public void add(FollowDTO followDTO) {
        //判断关注数量是否到达上线
        Long followNumber = getFollowNumber(followDTO.getFansId());
        if(followNumber>1000) throw new FollowException("关注数量已经达到上线");
        //检查传递类型
        Integer type = followDTO.getType();
        if(!(type==0||type==1)) throw new FollowException("没用该类型的目标");
        //同时还需要判断当前想要关注的用户是不是自己
        if (type==0&& Objects.equals(BaseContext.getCurrentId(), followDTO.getTargetId())) {
            //是同一个用户不能关注自己
            throw new FollowException("不能关注自己");
        }
        if(hasFollow(followDTO.getType(),followDTO.getFansId(),followDTO.getTargetId())){
            throw new FollowException("当前用户已经关注过");
        }
        //先转化成Follow类
        Follow follow= BeanUtil.copyProperties(followDTO,Follow.class);
        follow.setCreateTime(LocalDateTime.now());
        String lockKey= RedisConstant.LOCK_FOLLOW_KEY+follow.getType()+":"+follow.getTargetId()+":"+follow.getFansId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L, TimeUnit.SECONDS)){
                try {
                    //进行数据库操作
                    save(follow);
                    Long targetId = follow.getTargetId();
                    //要开启事务将目标的关注数加一
                    if(type==1){
                        shopService.incrFansNumber(targetId);
                    }else{
                        userService.incrFansNumber(targetId);
                    }
                    //删除缓存
                    if(type==1) redisTemplate.delete(RedisConstant.SHOP_KEY+ targetId);
                    //todo 发送异步消息进行收件箱拉取目标发布的动态
                    //postProducer.sendFollowMessage(follow);
                } finally {
                    lock.unlock();
                }
            }else{
                throw new FollowException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @GlobalTransactional
    public void cancelFollow(FollowDTO followDTO) {
        //todo 进行操作限流
        //检查传递类型
        Integer type = followDTO.getType();
        if(!(type==0||type==1)) throw new FollowException("没用该类型的目标");
        //先判断当前用户有没有权力来取消关注
        if (!Objects.equals(followDTO.getFansId(), BaseContext.getCurrentId())) {
            //不能进行取消关注的操作
            throw new FollowException("当前用户没有权力来取消关注");
        }
        if(!hasFollow(followDTO.getType(),followDTO.getFansId(),followDTO.getTargetId())){
            throw new FollowException("当前用户没有关注过");
        }
        //先转化成Follow类
        Follow follow = BeanUtil.copyProperties(followDTO,Follow.class);

        String lockKey= RedisConstant.LOCK_FOLLOW_KEY+follow.getType()+":"+follow.getTargetId()+":"+follow.getFansId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(1L,10L,TimeUnit.SECONDS)){
                try {
                    //进行取消关注
                    //要开启事务将目标的关注数加一
                    Long targetId = follow.getTargetId();
                    lambdaUpdate().eq(Follow::getType,type)
                            .eq(Follow::getFansId,follow.getFansId())
                            .eq(Follow::getTargetId, targetId)
                            .remove();
                    if(type==1){
                        shopService.decrFansNumber(targetId);
                    }else{
                        userService.decrFansNumber(targetId);
                    }
                    //删除缓存
                    if(type==1)redisTemplate.delete(RedisConstant.SHOP_KEY+ targetId);
                    //todo 发送异步消息进行收件箱除去目标发布的动态
                    //postProducer.sendCancelFollowMessage(follow);
                } finally {
                    lock.unlock();
                }
            }else{
                throw new FollowException("请勿重复操作");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PageResult<FollowVO> pageQuery(FollowPageQuery followPageQuery) {
        //判断查询目标是否存在
        Integer type = followPageQuery.getType();
        if(!(type==1||type==0)){
            throw new FollowException("目标类型错误");
        }
        //查询当前页的数据
        Page<Follow> page = lambdaQuery().eq(Follow::getType, type)
                .eq(Follow::getFansId, BaseContext.getCurrentId())
                .page(followPageQuery.toMpPage("id", true));
        //将Follow转成FollowVO
        List<FollowVO> followVOList = page.getRecords().stream()
                .map(follow -> BeanUtil.copyProperties(follow, FollowVO.class))
                .toList();
        //封装成PageResult类
        PageResult<FollowVO> pageResult = new PageResult<>();
        pageResult.setRecords(followVOList);
        pageResult.setTotal(page.getTotal());
        return pageResult;
    }

    @Override
    public boolean hasFollow(Integer type, Long userId, Long targetId) {
        //查询数据库，判断是否有这一条数据
        return lambdaQuery().eq(Follow::getType,type).eq(Follow::getFansId,userId).eq(Follow::getTargetId,targetId).exists();
    }

    @Override
    public List<Long> followWithShopIds(Long userId) {
        return lambdaQuery().eq(Follow::getFansId,userId).eq(Follow::getType,1).select(Follow::getTargetId)
                .list().stream().map(Follow::getTargetId).toList();
    }

    @Override
    public List<Long> followWithUserIds(Long userId) {
        return lambdaQuery().eq(Follow::getFansId,userId).eq(Follow::getType,0).select(Follow::getTargetId)
                .list().stream().map(Follow::getTargetId).toList();
    }

    @Override
    public List<Long> getByUserId(Long userId) {
        return lambdaQuery().eq(Follow::getFansId,userId).select(Follow::getTargetId)
                .list().stream().map(Follow::getTargetId).toList();
    }

    @Override
    public void deleteByShopId(Long shopId) {
        lambdaUpdate().eq(Follow::getTargetId,shopId).eq(Follow::getType,1).remove();
    }

    private Long getFollowNumber(Long fansId){
        //获取关注数
        return lambdaQuery().eq(Follow::getFansId,fansId).count();
    }
}