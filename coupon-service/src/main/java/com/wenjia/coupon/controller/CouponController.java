package com.wenjia.coupon.controller;


import com.wenjia.api.domain.dto.CouponDTO;
import com.wenjia.api.domain.vo.CouponVO;
import com.wenjia.api.service.CouponService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
@Tag(name = "优惠券接口")
public class CouponController {
    @DubboReference
    private CouponService couponService;

    @Operation(summary = "新增优惠券")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CouponDTO couponDTO){
        couponService.save(couponDTO);
        return Result.success();
    }

    @Operation(summary = "抢购优惠券")
    @PostMapping("/seckill")
    public Result<Long> seckill(@RequestParam Long couponId){
        Long orderId=couponService.seckill(couponId);
        return Result.success(orderId);
    }

    @Operation(summary = "查询商铺下的优惠券")
    @GetMapping
    public Result<List<CouponVO>> queryByShopId(@RequestParam Long shopId){
        List<CouponVO> couponVOS=couponService.queryByShopId(shopId);
        return Result.success(couponVOS);
    }

    @Operation(summary = "查询缓存中的优惠券的库存")
    @GetMapping("/stock")
    public Result<Integer> getStock(@RequestParam Long couponId){
        Integer count = couponService.getStock(couponId);
        return Result.success(count);
    }

    @Operation(summary = "删除优惠券，不能让用户来传递商铺id")
    @DeleteMapping
    public Result<Void> delete(@RequestParam Long couponId){
        couponService.delete(couponId);
        return Result.success();
    }

    @Operation(summary = "更改优惠券库存")
    @PutMapping("/stock")
    public Result<Void> updateStock(@RequestParam Long couponId,@RequestParam Integer stockChange){
        couponService.updateStock(couponId,stockChange);
        return Result.success();
    }
}
