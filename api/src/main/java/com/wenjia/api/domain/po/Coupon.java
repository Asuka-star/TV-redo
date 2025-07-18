package com.wenjia.api.domain.po;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
@TableName("coupon")
public class Coupon implements Serializable {
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime beginTime;
    //抢购开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endTime;
}
