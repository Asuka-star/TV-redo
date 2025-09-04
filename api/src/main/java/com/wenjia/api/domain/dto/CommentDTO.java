package com.wenjia.api.domain.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.naming.InsufficientResourcesException;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {
    //进行评论的用户名
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{6,12}$",message = "用户名有误")
    private String username;
    //被评论的目标类型
    @Min(value = 0,message = "目标类型有误")@Max(value = 2,message = "目标类型有误")
    private Integer type;
    //目标id
    @Min(1)
    private Long targetId;
    //评论内容
    @Size(min=1,max=500,message = "评论的长度有误")
    private String content;
}
