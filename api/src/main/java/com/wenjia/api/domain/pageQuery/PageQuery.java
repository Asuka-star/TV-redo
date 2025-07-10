package com.wenjia.api.domain.pageQuery;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.util.internal.StringUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQuery {
    @Min(value = 0,message = "页码不能小于0")
    @Max(value = 100,message = "页码不能大于100")
    private Integer page;
    @Min(value = 0,message = "单页大小不能小于0")
    @Max(value = 100,message = "单页大小不能大于100")
    private Integer pageSize;
    private Boolean isAsc;
    private String sortBy;

    public <T> Page<T> toMpPage(String defaultSortBy,boolean isAsc){
        if(StrUtil.isBlank(sortBy)){
            sortBy=defaultSortBy;
            this.isAsc=isAsc;
        }
        Page<T> page=new Page<>(this.page,pageSize);
        OrderItem orderItem=new OrderItem();
        orderItem.setAsc(this.isAsc);
        orderItem.setColumn(sortBy);
        page.addOrder(orderItem);
        return page;
    }
}
