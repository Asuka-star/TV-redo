package com.wenjia.coupon.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.po.Order;
import com.wenjia.api.domain.vo.OrderVO;
import com.wenjia.api.service.OrderService;
import com.wenjia.common.context.BaseContext;
import com.wenjia.coupon.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public List<OrderVO> getOwnedShops() {
        List<Order> orders = lambdaQuery().eq(Order::getUserId, BaseContext.getCurrentId()).list();
        return orders.stream().map(o -> {
            return OrderVO.builder()
                    .id(o.getId())
                    .couponId(o.getCouponId())
                    .status(o.getStatus())
                    .createTime(o.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }
}