package com.wenjia.api.domain.po;


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
public class Post implements Serializable {
    //帖子id
    private Long id;
    //发布者id
    private Long publisherId;
    //发布者名字
    private String publisherName;
    //发布者类型
    private Integer publisherType;
    //帖子类型
    private Integer type;
    //帖子标题
    private String title;
    //帖子对应的图片或视频的url
    private String urls;
    //内容
    private String content;
    //点赞数
    private Integer thumbNumber;
    //评论数
    private Integer commentNumber;
    //收藏数
    private Integer favoriteNumber;
    //发布时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
