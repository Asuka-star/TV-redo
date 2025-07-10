package com.wenjia.api.domain.pageQuery;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CommentPageQuery extends PageQuery {
    //目标类型
    @Min(value = 0,message = "目标类型有误")@Max(value = 2,message = "目标类型有误")
    private Integer type;
    //目标id
    @Min(1)
    private Long targetId;
    //当前登录的用户id
    @Min(1)
    private Long userId;
}
