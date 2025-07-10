package com.wenjia.coupon.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@Tag(name = "订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 查询当前用户名下的订单
     */
    @RequestMapping(value = "/order/owner", methodType = RequestMethod.GET)
    public Result<List<OrderVO>> getOwnedShops() {
        List<OrderVO> OrderVOS = orderService.getOwnedShops();
        return Result.success(OrderVOS);
    }
}
