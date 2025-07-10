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
public class ThumbVO implements Serializable {
    //点赞的id
    private Long id;
    //进行点赞的用户id
    private Long userId;
    //被点赞的对象类型
    private Integer type;
    //被点赞的对象id
    private Long targetId;
    //被点赞的内容
    private String content;
    //点赞的时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
