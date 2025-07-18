package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.po.Order;
import com.wenjia.api.domain.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Order> {
    //查询用户名下的订单
    List<OrderVO> getOwnedShops();
    //创建订单
    void create(Order order);
    //查询订单状态
    Integer getStatusById(Long id);
    //更新订单状态
    void updateStatus(Long id, Integer status);
    //订单支付
    void payment(Long orderId);
}
