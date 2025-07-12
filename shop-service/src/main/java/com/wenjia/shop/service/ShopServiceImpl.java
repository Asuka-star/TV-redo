package com.wenjia.shop.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.ShopDTO;
import com.wenjia.api.domain.pageQuery.ShopPageQuery;
import com.wenjia.api.domain.po.Comment;
import com.wenjia.api.domain.po.Coupon;
import com.wenjia.api.domain.po.Shop;
import com.wenjia.api.domain.vo.ShopVO;
import com.wenjia.api.service.*;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.ShopException;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.shop.mapper.ShopMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @DubboReference
    private ThumbService thumbService;
    @DubboReference
    private FollowService followService;
    @DubboReference
    private CouponService couponService;
    @DubboReference
    private CommentService commentService;

    private final RedisTemplate<String,Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public void register(ShopDTO shopDTO) {
        //封装成shop对象
        Shop shop = BeanUtil.copyProperties(shopDTO,Shop.class);
        //todo 还有es来进行查询
        //向数据库insert
        save(shop);
        //删除缓存
        deletePageCache();
    }

    @Override
    public PageResult<ShopVO> pageQuery(ShopPageQuery shopPageQuery) {
        //先查询缓存
        String key=getKeyByShopPageQuery(shopPageQuery);
        String value = (String) redisTemplate.opsForValue().get(key);
        //缓存穿透问题
        //这里不用在意穿透问题，因为我这里直接缓存的PageResult，就算是空数据，里面的数据也是没有的
        if("".equals(value)){
            return new PageResult<>(0L,null);
        }
        if(value!=null){
            return JSON.parseObject(value, new TypeReference<PageResult<ShopVO>>() {});
        }
        //缓存未击中，查询数据库
        //根据查询条件来查询对应shop表中的个数
        //进行加锁以免缓存击穿
        //获取分布式锁
        long count=0L;
        List<ShopVO> shopVOList = null;
        RLock lock = redissonClient.getLock(key);
        try {
            boolean tryLock = lock.tryLock(1L, 10L, TimeUnit.SECONDS);
            if (tryLock) {
                try {
                    //查询当前页的数据
                    Page<Shop> shopPage = lambdaQuery().like(StrUtil.isNotBlank(shopPageQuery.getName()),
                                    Shop::getName,
                                    shopPageQuery.getName())
                            .page(shopPageQuery.toMpPage("id", true));
                    count=shopPage.getTotal();
                    List<Shop> shopList = shopPage.getRecords();
                    //将Shop转化成ShopVO,还需要返回hasFollow和hasThumb(判断当前用户是否有进行登录)
                    shopVOList = new ArrayList<>();
                    Long userId = BaseContext.getCurrentId();
                    if (userId == null) {
                        //如果当前用户没有登录则直接将hasFollow和hasThumb设置为false,不设置也行,默认就是false
                        for (Shop shop : shopList) {
                            //转化
                            ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
                            shopVO.setHasFollow(false);
                            shopVO.setHasThumb(false);
                            shopVOList.add(shopVO);
                        }
                    } else {
                        //当前已经有用户登录了,需要去查找关注表和点赞表来给hasFollow和hasThumb赋值
                        //查询用户点赞的所有商铺id
                        List<Long> thumbWithShopIds = thumbService.thumbWithShopIds(userId);
                        //查询用户关注的所有商铺id
                        List<Long> followWithShopIds = followService.followWithShopIds(userId);
                        for (Shop shop : shopList) {
                            //判断当前用户是否有关注或者点赞了该商铺
                            Boolean hasThumb = thumbWithShopIds.contains(shop.getId());
                            Boolean hasFollow = followWithShopIds.contains(shop.getId());
                            //转化
                            ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
                            shopVO.setHasFollow(hasFollow);
                            shopVO.setHasThumb(hasThumb);
                            shopVOList.add(shopVO);
                        }

                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取锁失败");
            throw new RuntimeException(e);
        }
        //封装返回结果
        PageResult<ShopVO> shopVOPageResult = new PageResult<>();
        shopVOPageResult.setTotal(count);
        shopVOPageResult.setRecords(shopVOList);
        //缓存当前分页查询的结果到缓存中
        redisTemplate.opsForValue().set(key,JSON.toJSONString(shopVOPageResult),
                RedisConstant.EXPIRE_TIME,RedisConstant.TIME_UNIT);
        return shopVOPageResult;
    }

    @Override
    @GlobalTransactional
    public void deleteByShopId(Long ShopId) {
        //先判断当前用户是否是创建这个商铺的用户
        Shop shop = lambdaQuery().eq(Shop::getId,ShopId).one();
        if(shop==null) throw new ShopException("商铺不存在无法进行删除");
        if (!Objects.equals(shop.getOwnerId(), BaseContext.getCurrentId())) {
            //当前用户不是店主，不能删除该店铺，抛出异常
            throw new ShopException("您不是此商铺的店主，无法删除此商铺");
        }
        //判断商铺名下还有没有正在抢购的优惠券
        List<Coupon> coupons = couponService.getByShopId(shop.getId());
        for(Coupon coupon:coupons){
            LocalDateTime beginTime = coupon.getBeginTime();
            LocalDateTime endTime = coupon.getEndTime();
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(beginTime) && now.isBefore(endTime)) {
                throw new ShopException("当前商铺还有正在抢购的优惠券：" + coupon.getName());
            }
        }
        //这里需要分布式事务，还需要删除这个商铺下面的优惠券，评论，关注，点赞
        //进行删除店铺操作
        removeById(shop);
        commentService.deleteByShopId(shop.getId());
        couponService.deleteByShopId(shop.getId());
        followService.deleteByShopId(shop.getId());
        thumbService.deleteByShopId(shop.getId());
        redisTemplate.delete(RedisConstant.SHOP_KEY+shop.getId());
        //还要删除商铺分页查询的所有缓存
        deletePageCache();
    }

    private void deletePageCache() {
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

    @Override
    public ShopVO getByIdWithCache(Long id) {
        //查询缓存
        String key=RedisConstant.SHOP_KEY + id;
        String value =(String) redisTemplate.opsForValue().get(key);
        if("".equals(value)){
            //防止缓存穿透
            throw new ShopException("没有这个商铺");
        }
        if(value!=null){
            return BeanUtil.copyProperties(JSON.parseObject(value,Shop.class), ShopVO.class);
        }
        //查询数据库
        Shop shop = lambdaQuery().eq(Shop::getId,id).one();
        if(shop==null){
            //没有这个商铺，抛出异常，告诉前端没有这个商铺，同时缓存空数据
            redisTemplate.opsForValue().set(key,"",10L,TimeUnit.SECONDS);
            throw new ShopException("没有找到ID为" + id + "的商铺");
        }
        redisTemplate.opsForValue().set(key,JSON.toJSONString(shop),RedisConstant.EXPIRE_TIME,TimeUnit.MINUTES);
        //转化成shopVO
        return BeanUtil.copyProperties(shop, ShopVO.class);
    }


    @Override
    public List<ShopVO> getOwnedShops() {
        List<Shop> shops = lambdaQuery().eq(Shop::getId,BaseContext.getCurrentId()).list();
        List<ShopVO> shopVOS = new ArrayList<>();
        //将shop转化成shopVO
        for (Shop shop : shops) {
            ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
            shopVOS.add(shopVO);
        }
        return shopVOS;
    }

    @Override
    public void incrCommentNumber(Long shopId) {
        lambdaUpdate().setSql("comment_number=comment_number+1").eq(Shop::getId,shopId).update();
    }

    @Override
    public void decrCommentNumber(Long shopId) {
        lambdaUpdate().setSql("comment_number=comment_number-1")
                .eq(Shop::getId,shopId).ge(Shop::getCommentNumber,0).update();
    }

    @Override
    public void incrThumbNumber(Long shopId) {
        lambdaUpdate().setSql("thumb_number=thumb_number+1").eq(Shop::getId,shopId).update();
    }

    @Override
    public void decrThumbNumber(Long shopId) {
        lambdaUpdate().setSql("thumb_number=thumb_number-1")
                .eq(Shop::getId,shopId).ge(Shop::getThumbNumber,0).update();
    }

    /**
     * 通过ShopPageQuery类中的参数来获取对应的key
     */
    public static String getKeyByShopPageQuery(ShopPageQuery shopPageQuery) {
        return "shopPage:"
                + shopPageQuery.getPage() + ":"
                + shopPageQuery.getPageSize() + ":"
                + shopPageQuery.getSortBy() + ":"
                + shopPageQuery.getIsAsc() + ":"
                + shopPageQuery.getUserId() + ":"
                + shopPageQuery.getName();
    }
}
