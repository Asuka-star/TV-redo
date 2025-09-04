package com.wenjia.api.domain.dto;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO implements Serializable {
    //发布者id
    private Long publisherId;
    //发布者名字
    @Size(max=20)
    private String publisherName;
    //发布者类型
    private Integer publisherType;
    //帖子类型
    private Integer type;
    //帖子标题
    @Size(max = 20)
    private String title;
    //帖子对应的图片或视频的url
    @Size(max=2048)
    private String urls;
    //内容
    @Size(max = 2048)
    private String content;
}
