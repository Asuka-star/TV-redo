package com.wenjia.api.domain.vo;


import com.alibaba.fastjson2.annotation.JSONField;
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
public class CommentVO implements Serializable {
    //评论id
    private Long id;
    //进行评论的用户id
    private Long userId;
    //进行评论的用户名
    private String username;
    //评论的内容
    private String content;
    //所获点赞数
    private Integer thumbNumber;
    //表示当前用户是否已经关注了发布者
    private Boolean hasFollow;
    //表示当前用户是否已经点赞了该评论
    private Boolean hasThumb;
    //评论的时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
