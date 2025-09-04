package com.wenjia.api.domain.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowDTO implements Serializable {
    //进行关注的用户id
    @Min(1)
    private Long fansId;
    //目标类型
    @Min(0)@Max(1)
    private Integer type;
    //目标id
    @Min(1)
    private Long targetId;
    //目标姓名
    @Size(min=0,max=20)
    private String targetName;
}
