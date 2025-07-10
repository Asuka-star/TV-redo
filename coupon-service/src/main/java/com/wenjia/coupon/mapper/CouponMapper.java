package com.wenjia.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenjia.api.domain.po.Coupon;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CouponMapper extends BaseMapper<Coupon> {
    @Update("update coupon set stock=stock+#{stockChange} where id=#{couponId}")
    void updateStock(@Param("couponId") Long couponId,@Param("stockChange") Integer stockChange);
}
