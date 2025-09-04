package com.wenjia.api.domain.vo;


import com.alibaba.fastjson2.annotation.JSONField;
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
public class ShopVO implements Serializable {
    private Long id;
    private String name;
    private Long ownerId;
    private String address;
    private Integer commentNumber;
    private Integer thumbNumber;
    private Integer fansNumber;
    //表示当前用户是否已经关注了当前商铺
    private Boolean hasFollow;
    //表示当前用户是否已经点赞了当前用户
    private Boolean hasThumb;
    @JSONField(format = "HH:mm:ss")
    private LocalTime beginTime;
    @JSONField(format = "HH:mm:ss")
    private LocalTime endTime;
}
