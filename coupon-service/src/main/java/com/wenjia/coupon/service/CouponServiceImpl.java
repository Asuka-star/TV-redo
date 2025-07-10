package com.wenjia.coupon.service;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.CouponDTO;
import com.wenjia.api.domain.po.Coupon;
import com.wenjia.api.domain.po.Order;
import com.wenjia.api.domain.vo.CouponVO;
import com.wenjia.api.domain.vo.ShopVO;
import com.wenjia.api.service.CouponService;
import com.wenjia.api.service.OrderService;
import com.wenjia.api.service.ShopService;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.BaseException;
import com.wenjia.common.exception.CouponException;
import com.wenjia.coupon.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@DubboService
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @DubboReference
    private ShopService shopService;
    @DubboReference
    private OrderService orderService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    //todo 消息发送
    //private OrderProducer orderProducer;

    //todo sentinel 的限流操作
    /*public CouponServiceImpl(){
        initFlowRules();
    }*/

    @Override
    public void save(CouponDTO couponDTO) {
        checkFormat(couponDTO);
        //转化成Coupon类
        Coupon coupon = BeanUtil.copyProperties(couponDTO, Coupon.class);
        save(coupon);
        long expireTime = getExpireTimeByEndTime(couponDTO.getEndTime());
        //进行缓存当前优惠券的信息，(主要是缓存优惠券的时间和所属的商铺id)
        redisTemplate.opsForValue().set(RedisConstant.COUPON_KEY + coupon.getId(),
                JSON.toJSONString(coupon),
                expireTime, TimeUnit.SECONDS);
        //缓存当前优惠券的库存信息
        redisTemplate.opsForValue().set(RedisConstant.COUPON_STOCK_KEY + coupon.getId(),
                couponDTO.getStock(),
                expireTime, TimeUnit.SECONDS);
    }

    @Override
    public List<CouponVO> queryByShopId(Long shopId) {
        return lambdaQuery().eq(Coupon::getShopId, shopId).list()
                .stream().map(coupon -> BeanUtil.copyProperties(coupon, CouponVO.class))
                .toList();
    }

    @Override
    public Integer getStock(Long couponId) {
        //只查询正在抢购的优惠券
        String key = RedisConstant.COUPON_STOCK_KEY + couponId;
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new CouponException("缓存异常,没有id为" + couponId + "的优惠券缓存");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CouponException("缓存异常,id为" + couponId + "的优惠券缓存存入的值有误");
        }
    }

    public Coupon getById(Long couponId) {
        //先查询缓存
        String key = RedisConstant.COUPON_KEY + couponId;
        String value = (String) redisTemplate.opsForValue().get(key);
        if (value != null) {
            return value.isEmpty() ? null : JSON.parseObject(value, Coupon.class);
        }
        //缓存未击中，查询数据库
        //进行加锁以免缓存击穿
        RLock lock = redissonClient.getLock(couponId.toString());
        try {
            if (lock.tryLock(1L, 10L, TimeUnit.SECONDS)) {
                try {
                    //双重检查：获取锁后再次确认缓存状态
                    String doubleCheckValue = (String) redisTemplate.opsForValue().get(key);
                    if (doubleCheckValue != null) {
                        return doubleCheckValue.isEmpty() ? null : JSON.parseObject(doubleCheckValue, Coupon.class);
                    }
                    //查询数据库
                    Coupon coupon =lambdaQuery().eq(Coupon::getId,couponId).one();
                    //缓存数据
                    if (coupon == null) {
                        redisTemplate.opsForValue().set(
                                key,
                                "",
                                180L,
                                TimeUnit.SECONDS
                        );
                    } else {
                        redisTemplate.opsForValue().set(
                                key,
                                coupon,
                                getExpireTimeByEndTime(coupon.getEndTime()),
                                TimeUnit.SECONDS
                        );

                    }
                    return coupon;
                } finally {
                    // 确保释放锁
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //重试多次失败后仍然不行就抛出异常
        throw new BaseException("系统繁忙，请稍后重试");
    }

    @Override
    public Long seckill(Long couponId) {
        //查询优惠券
        Coupon coupon = getById(couponId);
        //查找到之后需要判断是否存在这么一个优惠券和是否是在抢购时间
        if (coupon == null) {
            throw new CouponException("不存在id为" + couponId + "的优惠券");
        }
        LocalDateTime beginTime = coupon.getBeginTime();
        LocalDateTime endTime = coupon.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        if (beginTime.isAfter(now)) {
            throw new CouponException("当前优惠券抢购未开始");
        }
        if (endTime.isBefore(now)) {
            throw new CouponException("当前优惠券抢购已结束");
        }
        //通过redis的lua脚本来解决超卖和一人一单的问题
        //当前用户id
        Long userId = BaseContext.getCurrentId();
        int res = checkSeckill(couponId, userId, getExpireTimeByEndTime(coupon.getEndTime()));
        if (res == 1) {
            throw new CouponException("库存不足,已经被抢购完了");
        } else if (res == 2) {
            throw new CouponException("您已经买过该优惠券了");
        }
        //返回零说明抢购成功
        //生成订单id
        Long orderId = RedisId.createId(RedisConstant.ORDER_KEY);
        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .couponId(couponId)
                .createTime(LocalDateTime.now())
                .build();
        //order创建的数据库操作并扣减了库存
        try {
            orderProducer.sendOrderCreateMessage(order);
        } catch (Exception e) {
            //信息发送失败进行缓存数据的回退
            JedisUtil.updateStock(order.getCouponId(), order.getUserId());
            throw new CouponException("RRR服务繁忙请稍后再试");
        }
        return orderId;
    }

//    /**
//     * 初始化限流规则
//     */
//    private void initFlowRules() {
//        //限流
//        FlowRule rule = new FlowRule("secKill")
//                .setGrade(RuleConstant.FLOW_GRADE_QPS)
//                .setCount(1000)
//                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
//                .setMaxQueueingTimeMs(200);
//        FlowRuleManager.loadRules(Collections.singletonList(rule));
//        //熔断
//        DegradeRule degradeRule = new DegradeRule("secKill")
//                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
//                .setCount(2000)
//                .setTimeWindow(3)
//                .setMinRequestAmount(10)
//                .setStatIntervalMs(3000);
//        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));
//    }

    /**
     * 删除优惠券，不能让用户来传递商铺id
     */
    @Override
    public void delete(Long couponId) {
        Coupon coupon = checkValid(couponId);
        //如果优惠券正在抢购则无法进行删除
        LocalDateTime beginTime = coupon.getBeginTime();
        LocalDateTime endTime = coupon.getEndTime();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
        if (beginTime.isAfter(now) && endTime.isBefore(now)) {
            throw new CouponException("优惠券正在进行抢购,无法进行删除");
        }
        //可以进行删除，进行延迟双删,需要删除优惠券信息和优惠券库存信息
        String couponKey = RedisConstant.COUPON_KEY + couponId;
        String stockKey = RedisConstant.COUPON_STOCK_KEY + couponId;
        JedisUtil.delete(couponKey);
        JedisUtil.delete(stockKey);
        couponDao.delete(couponId);
        JedisUtil.deleteWithDelay(couponKey);
        JedisUtil.deleteWithDelay(stockKey);
    }

    /**
     * 修改数据库中的优惠券库存
     */
    @Override
    public void updateStock(Long couponId, Integer stockChange) {
        if (Math.abs(stockChange) > 1000000) {
            throw new CouponException("库存变化量过大");
        }
        //店主一次只能进行一次
        ILock lock = new RedisLock(RedisConstant.UPDATE_STOCK_KEY + couponId);
        if (lock.tryLock(2L)) {
            try {
                Coupon coupon = checkValid(couponId);
                //判断优惠券抢购时间是否已经结束了
                if (coupon.getEndTime().isBefore(LocalDateTime.now(ZoneOffset.ofHours(8)))) {
                    throw new CouponException("当前优惠券已经结束抢购，无法再进行库存修改");
                }
                if (getStock(couponId) + stockChange > 1000000) throw new CouponException("优惠券库存超出限制");
                //修改缓存中的库存
                updateStockWithRedis(couponId, stockChange);
                //修改数据库中的缓存
                try {
                    couponDao.updateStock(couponId, stockChange);
                } catch (Exception e) {
                    //数据库中的库存更新失败，需要将缓存中的库存回到最开始
                    updateStockWithRedis(couponId, -stockChange);
                    throw new CouponException("更新库存失败");
                }
            } finally {
                lock.unLock();
            }
        } else {
            //没有获取到锁
            throw new CouponException("请勿重复操作");
        }
    }

    /**
     * 修改缓存中的优惠券库存
     */
    public void updateStockWithRedis(Long couponId, Integer stockChange) {
        try (Jedis jedis = JedisConnectionPool.getJedis()) {
            //先修改缓存中的库存值，使用lua脚本进行操作
            String script = "local stock =redis.call('get',KEYS[1])\n" +
                    "if not(stock) then \n" +
                    "return 1\n" +
                    "end\n" +
                    "local stockNum=tonumber(stock)\n" +
                    "local newStock=stockNum+tonumber(ARGV[1])\n" +
                    "if newStock<0 then\n" +
                    "return 2\n" +
                    "end\n" +
                    "local ttl = redis.call('ttl',KEYS[1]);\n" +
                    "redis.call('set',KEYS[1],newStock)\n" +
                    "if tonumber(ttl)>0 then\n" +
                    "redis.call('expire',KEYS[1],ttl)\n" +
                    "end\n" +
                    "return 0";
            Long res = (Long) jedis.eval(script,
                    1,
                    RedisConstant.COUPON_STOCK_KEY + couponId,
                    stockChange.toString());
            if (res == 1) {
                throw new CouponException("优惠券已经被删除了");
            } else if (res == 2) {
                throw new CouponException("优惠券库存不足以减少输入的数量");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查当前用户操作的合法性
     */
    private Coupon checkValid(Long couponId) {
        //先查询该优惠券是否存在,并通过优惠券id来查询优惠券所属的商铺id
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new CouponException("不存在id为" + couponId + "的优惠券");
        }
        //检查当前用户是不是店主能不能删
        ShopVO shopVO = shopService.getByIdWithCache(coupon.getShopId());
        Long userId = BaseContext.getCurrentId();
        if (!Objects.equals(userId, shopVO.getOwnerId())) {
            throw new CouponException("您不是店主，无法进行此操作");
        }
        return coupon;
    }

    /**
     * 检查优惠券库存和用户是否已经购买过了
     */
    private int checkSeckill(Long couponId, Integer userId, Long expireTime) {
        //需要记录是否购买过优惠券的用户id的缓存，过期时间
        String script = "if tonumber(redis.call('get',KEYS[1])) <=0 then\n" +
                "return 1\n" +
                "end\n" +
                "if redis.call('sismember',KEYS[2],ARGV[1]) ==1 then\n" +
                "return 2\n" +
                "end\n" +
                "redis.call('decr',KEYS[1])\n" +
                "redis.call('sadd',KEYS[2],ARGV[1])\n" +
                "if tonumber(redis.call('ttl',KEYS[2])) ==-1 then\n" +
                "redis.call('expire',KEYS[2],tonumber(ARGV[2]))\n" +
                "end\n" +
                "return 0";
        try (Jedis jedis = JedisConnectionPool.getJedis()) {
            Long res = (Long) jedis.eval(script,
                    2,
                    RedisConstant.COUPON_STOCK_KEY + couponId,
                    RedisConstant.SECKILL_KEY + couponId,
                    userId.toString(),
                    expireTime.toString());
            //就算是null，也刚好会被catch
            return res.intValue();
        } catch (Exception e) {
            throw new RuntimeException("抢购优惠券的redis判断出现异常");
        }
    }

    /**
     * 检验用户传递的值
     */
    private void checkFormat(CouponDTO couponDTO) {
        //获取字段信息
        BigDecimal discountRate = couponDTO.getDiscountRate();
        BigDecimal fullAmount = couponDTO.getFullAmount();
        BigDecimal reduceAmount = couponDTO.getReduceAmount();
        LocalDateTime beginTime = couponDTO.getBeginTime();
        LocalDateTime endTime = couponDTO.getEndTime();
        Integer type = couponDTO.getType();
        BigDecimal maxDiscountRate = new BigDecimal(100);
        BigDecimal maxAmount = new BigDecimal(1000);
        BigDecimal zero = new BigDecimal(0);
        switch (type) {
            case 1:
                if (discountRate == null || discountRate.compareTo(maxDiscountRate) > 0
                        || discountRate.compareTo(zero) < 0 || fullAmount != null || reduceAmount != null) {
                    throw new CouponException("输入的折扣券信息有误");
                }
                break;
            case 2:
                if (reduceAmount == null || reduceAmount.compareTo(maxAmount) > 0
                        || discountRate != null || fullAmount != null || reduceAmount.compareTo(zero) < 0) {
                    throw new CouponException("输入的立减券信息有误，最大为1000");
                }
                break;
            case 3:
                if (reduceAmount == null || fullAmount == null
                        || fullAmount.compareTo(maxAmount) > 0 || reduceAmount.compareTo(maxAmount) > 0
                        || discountRate != null || reduceAmount.compareTo(fullAmount) > 0) {
                    throw new CouponException("输入的满减券信息有误，最大为1000");
                }
                break;
            default:
                throw new CouponException("没有此类型的优惠券");
        }
        //检查时间(只以当前时间的年月日时分进行判断，不加上秒)
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        if (beginTime.isAfter(endTime) || beginTime.isBefore(now)) {
            throw new CouponException("优惠券的抢购时间有误");
        }
    }

    /**
     * 通过抢购结束时间获取缓存过期时间
     */
    private Long getExpireTimeByEndTime(LocalDateTime endTime) {
        //还需要判断一下当前优惠券的抢购时间是否已经结束了
        if (endTime.isBefore(LocalDateTime.now(ZoneOffset.ofHours(8)))) {
            //已经结束了,返回三分钟
            return 180L;
        }
        //比优惠券结束时间多一分钟
        return endTime.plusMinutes(1L).toEpochSecond(ZoneOffset.ofHours(8))
                - LocalDateTime.now(ZoneOffset.ofHours(8)).toEpochSecond(ZoneOffset.ofHours(8));
    }
}