package com.wenjia.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderVO implements Serializable {
    //订单id
    private Long id;
    //优惠券id
    private Long couponId;
    //订单状态
    private Integer status;
    //创建时间
    private LocalDateTime createTime;
}
