package com.wenjia.coupon.mq.producer;

import com.wenjia.api.domain.po.Order;
import com.wenjia.common.constant.MQConstant;
import com.wenjia.common.exception.CouponException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送创建订单消息
     * @param order
     */

    /*将订单创建整合上了rocketmq，服务端使用的是5.3.2版本
    然后spring整合的rocketmq不支持5.x版本，同时他的2.3.4版本还有问题，所以最终选择了使用4.9.7的客户端连接5.3.2的服务端
    但是其实也可以只用官网提供的最原始的依赖，但是那个我在原来的TV上面使用了，这里就不用了，而且感觉启动速度慢*/
    public void sendOrderCreateMessage(Order order) {
        rocketMQTemplate.asyncSend(MQConstant.ORDER_CREATE_TOPIC + ":" + MQConstant.ORDER_CREATE_TAG,
                order, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("创建订单({})消息发送成功", order.getId());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        throw new CouponException("创建订单消息发送失败" + order.getId());
                    }
                });
    }

    public void sendOrderCheckMessage(Order order){
        rocketMQTemplate.syncSend(MQConstant.ORDER_CHECK_TOPIC+":"+MQConstant.ORDER_CHECK_TAG,
                MessageBuilder.withPayload(order).build(),3000,5);
    }
}
