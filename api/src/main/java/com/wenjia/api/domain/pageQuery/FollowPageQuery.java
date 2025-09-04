package com.wenjia.api.domain.pageQuery;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FollowPageQuery extends PageQuery {
    //指定查询目标类型
    private Integer type;
}
