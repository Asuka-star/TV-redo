package com.wenjia.coupon.controller;


import com.wenjia.api.domain.vo.OrderVO;
import com.wenjia.api.service.OrderService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Tag(name = "订单接口")
public class OrderController {
    @DubboReference
    private OrderService orderService;

    @GetMapping("/owner")
    @Operation(summary = "查询当前用户名下的订单")
    public Result<List<OrderVO>> getOwnedShops() {
        List<OrderVO> OrderVOS = orderService.getOwnedShops();
        return Result.success(OrderVOS);
    }

    @PutMapping("/payment")
    @Operation(summary = "订单支付")
    public Result paymentWithOrder(Long orderId) {
        orderService.payment(orderId);
        return Result.success();
    }
}
