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
public class FollowVO implements Serializable {
    //id
    private Long id;
    //进行关注的粉丝
    private Long fansId;
    //目标类型
    private Integer type;
    //目标id
    private Long targetId;
    //目标姓名
    private String targetName;
    //关注时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
