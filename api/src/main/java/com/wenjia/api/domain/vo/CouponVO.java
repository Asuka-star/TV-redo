package com.wenjia.api.domain.vo;


import com.alibaba.fastjson2.annotation.JSONField;
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
public class CouponVO implements Serializable {
    //优惠券id
    private Long id;
    //商铺id
    private Long shopId;
    //优惠券类型
    private Integer type;
    //优惠券名字
    private String name;
    //优惠券描述
    private String description;
    //优惠券库存
    private Integer stock;
    //折扣率
    private BigDecimal discountRate;
    //满减门槛
    private BigDecimal fullAmount;
    //减免金额
    private BigDecimal reduceAmount;
    //抢购结束时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    //抢购开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
