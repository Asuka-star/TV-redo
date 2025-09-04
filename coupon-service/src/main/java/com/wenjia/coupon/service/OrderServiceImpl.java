package com.wenjia.coupon.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.po.Order;
import com.wenjia.api.domain.vo.OrderVO;
import com.wenjia.api.service.OrderService;
import com.wenjia.common.constant.OrderConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.OrderException;
import com.wenjia.common.exception.PaymentException;
import com.wenjia.coupon.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@DubboService
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public List<OrderVO> getOwnedShops() {
        List<Order> orders = lambdaQuery().eq(Order::getUserId, BaseContext.getCurrentId()).list();
        return orders.stream().map(o -> BeanUtil.copyProperties(o,OrderVO.class)).collect(Collectors.toList());
    }

    @Override
    public void create(Order order) {
        save(order);
    }

    @Override
    public Integer getStatusById(Long id) {
        Order order = lambdaQuery().eq(Order::getId, id).select(Order::getStatus).one();
        if(order==null) throw new OrderException("订单不存在"+id);
        return order.getStatus();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        lambdaUpdate().set(Order::getStatus,status).eq(Order::getId,id).update();
    }

    @Override
    public void payment(Long orderId) {
        //看一下用户用没有支付这一条订单的权限
        Order order=getById(orderId);
        if(order==null){
            throw new PaymentException("没有这个订单，请稍等");
        }
        if(!Objects.equals(order.getUserId(), BaseContext.getCurrentId())){
            throw new PaymentException("用户没有权力支付这一条订单");
        }
        if(Objects.equals(OrderConstant.PAID, order.getStatus())){
            throw new PaymentException("该订单已支付过了");
        }else if(Objects.equals(order.getStatus(), OrderConstant.PAYMENT_TIMEOUT)){
            throw new PaymentException("订单已经超过了支付时间");
        }
        try {
            //修改订单状态为已支付
            updateStatus(orderId, OrderConstant.PAID);
        } catch (Exception e) {
            throw new PaymentException("服务器内部出错，支付失败");
        }
    }
}