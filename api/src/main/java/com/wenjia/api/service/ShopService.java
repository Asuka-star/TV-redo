package com.wenjia.api.service;

import com.wenjia.api.domain.vo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.ShopDTO;
import com.wenjia.api.domain.pageQuery.ShopPageQuery;
import com.wenjia.api.domain.po.Shop;
import com.wenjia.api.domain.vo.ShopVO;

import java.util.List;

public interface ShopService extends IService<Shop> {
    //注册商铺
    void register(ShopDTO shopDTO);

    //分页查询商铺
    PageResult<ShopVO> pageQuery(ShopPageQuery shopPageQuery);

    //根据id删除商铺
    void deleteByShopId(Long shopId);

    //根据id查找商铺
    ShopVO getByIdWithCache(Long id);

    //查询用户名下的商铺
    List<ShopVO> getOwnedShops();
}
