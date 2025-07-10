package com.wenjia.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.CouponDTO;
import com.wenjia.api.domain.po.Coupon;
import com.wenjia.api.domain.vo.CouponVO;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    //新增优惠券
    void save(CouponDTO couponDTO);
    //查询商铺下的全部优惠券
    List<CouponVO> queryByShopId(Long shopId);
    //获取库存
    Integer getStock(Long couponId);
    //秒杀优惠券
    Long seckill(Long couponId);
    //删除优惠券
    void delete(Long couponId);
    //修改优惠券的库存
    void updateStock(Long couponId, Integer stockChange);
    //判断商铺名下还有没有正在抢购的优惠券
    List<Coupon> getByShopId(Long shopId);
}
