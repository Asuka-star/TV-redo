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
@TableName("comment")
public class Comment implements Serializable {
    //评论id
    @TableId
    private Long id;
    //进行评论的用户id
    private Long userId;
    //进行评论的用户名
    private String username;
    //被评论的目标类型
    private Integer type;
    //目标的id
    private Long targetId;
    //评论的内容
    private String content;
    //所获点赞数
    private Integer thumbNumber;
    //评论的时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
