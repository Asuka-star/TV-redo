package com.wenjia.api.domain.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThumbDTO implements Serializable {
    //被点赞的对象类型
    @Min(value = 0,message = "对象类型错误")
    @Max(value = 2,message = "对象类型错误")
    private Integer type;
    //被点赞的对象id
    @Min(1)
    private Long targetId;
    //被点赞的内容
    @Size(max = 500,message = "长度不能超过500")
    private String content;
}
