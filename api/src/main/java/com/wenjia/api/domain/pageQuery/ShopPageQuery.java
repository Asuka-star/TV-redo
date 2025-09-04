package com.wenjia.api.domain.pageQuery;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShopPageQuery extends PageQuery {
    //根据名字来模糊查询
    @Size(max = 20)
    private String name;
    //当前分页查询商铺的用户id
    private Long userId;
}