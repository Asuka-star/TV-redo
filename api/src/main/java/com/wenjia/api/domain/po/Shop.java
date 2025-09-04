package com.wenjia.api.domain.po;


import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("shop")
public class Shop implements Serializable {
    //商铺id
    @TableId
    private Long id;
    //商铺名字
    private String name;
    //店主id
    private Long ownerId;
    //地址
    private String address;
    //所获评论数
    private Integer commentNumber;
    //所获点赞数
    private Integer thumbNumber;
    //粉丝数
    private Integer fansNumber;
    //营业开始时间
    @JSONField(format = "HH:mm:ss")
    private LocalTime beginTime;
    //营业结束时间
    @JSONField(format = "HH:mm:ss")
    private LocalTime endTime;
}
