package com.wenjia.api.domain.po;


import com.alibaba.fastjson2.annotation.JSONField;
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
@TableName("thumb")
public class Thumb implements Serializable {
    //点赞的id
    @TableId
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
