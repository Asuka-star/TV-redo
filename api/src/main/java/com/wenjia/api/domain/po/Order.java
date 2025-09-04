package com.wenjia.api.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("orders")
public class Order implements Serializable {
    //订单id
    @TableId
    private Long id;
    //下单用户id
    private Long userId;
    //优惠券id
    private Long couponId;
    //订单状态
    private Integer status;
    //创建时间
    private LocalDateTime createTime;
}
