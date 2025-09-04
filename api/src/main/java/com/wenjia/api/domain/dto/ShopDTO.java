package com.wenjia.api.domain.dto;


import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ShopDTO implements Serializable {
    //商铺名字
    @Size(max=20,message = "商铺名字长度要在0-20内")
    private String name;
    //商铺地址
    @Size(max=100,message = "商铺地址长度要在0-100内")
    private String address;
    //营业开始时间
    @JSONField(format = "HH:mm:ss")
    @NotNull
    private LocalTime beginTime;
    //营业结束时间
    @NotNull
    @JSONField(format = "HH:mm:ss")
    private LocalTime endTime;
}
