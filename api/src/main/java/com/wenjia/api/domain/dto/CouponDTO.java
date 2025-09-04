package com.wenjia.api.domain.dto;


import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDTO implements Serializable {
    //商铺id
    private Long shopId;
    //优惠券类型
    private Integer type;
    //优惠券名字
    private String name;
    //优惠券描述
    private String description;
    //优惠券库存
    @Min(0)
    private Integer stock;
    //折扣率
    private BigDecimal discountRate;
    //满减门槛
    private BigDecimal fullAmount;
    //减免金额
    private BigDecimal reduceAmount;
    //抢购结束时间
    private LocalDateTime beginTime;
    //抢购开始时间
    private LocalDateTime endTime;
}
