package com.wenjia.coupon.mq.consumer;

import com.wenjia.api.domain.po.Order;
import com.wenjia.api.service.CouponService;
import com.wenjia.api.service.OrderService;
import com.wenjia.common.constant.MQConstant;
import com.wenjia.common.constant.OrderConstant;
import com.wenjia.common.exception.OrderException;
import com.wenjia.common.util.RedisUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@RocketMQMessageListener(consumerGroup = "OrderCheckConsumer", topic = MQConstant.ORDER_CHECK_TOPIC, selectorExpression = MQConstant.ORDER_CHECK_TAG)
public class OrderCheckConsumer implements RocketMQListener<Order> {
    private final RedisUtil redisUtil;
    @DubboReference
    private OrderService orderService;
    @DubboReference
    private CouponService couponService;
    @Override
    @GlobalTransactional
    public void onMessage(Order order) {
        //判断订单是否超时
        try {
            Long id = order.getId();
            Integer status = orderService.getStatusById(id);
            if(Objects.equals(status, OrderConstant.UNPAID)){
                orderService.updateStatus(id,OrderConstant.PAYMENT_TIMEOUT);
                couponService.incrStock(order.getCouponId());
                //修改缓存,优惠券可能刚好结束了，导致缓存修改失败，所以要catch一下
                try {
                    redisUtil.rollBackStock(order.getCouponId(),order.getUserId());
                } catch (Exception e) {
                    log.warn("优惠券{}缓存已过期",order.getCouponId());
                }
                //todo 然后再是post的推拉，读扩散，写扩散
                log.info("订单{}检查消费成功",id);
            }
        } catch (Exception e) {
            throw new OrderException("订单检查消费失败"+order.getId());
        }
    }
}