package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.po.Order;
import com.wenjia.api.domain.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Order> {
    //查询用户名下的订单
    List<OrderVO> getOwnedShops();
}
