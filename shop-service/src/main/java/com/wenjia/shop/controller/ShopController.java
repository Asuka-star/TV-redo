package com.wenjia.shop.controller;


import com.wenjia.api.domain.dto.ShopDTO;
import com.wenjia.api.domain.pageQuery.ShopPageQuery;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.domain.vo.ShopVO;
import com.wenjia.api.service.ShopService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@Tag(name = "商铺接口")
public class ShopController {
    @DubboReference
    private ShopService shopService;

    @Operation(summary = "商铺注册")
    @PostMapping
    public Result<Void> register(@Valid @RequestBody ShopDTO shopDTO) {
        shopService.register(shopDTO);
        return Result.success();
    }

    @Operation(summary = "分页查询商铺")
    @GetMapping("/page")
    public Result<PageResult<ShopVO>> page(@Valid ShopPageQuery shopPageQuery) {
        PageResult<ShopVO> shopVOPageResult = shopService.pageQuery(shopPageQuery);
        //返回值
        return Result.success(shopVOPageResult);
    }

    @Operation(summary = "根据商铺id来删除商铺")
    @DeleteMapping
    public Result<Void> deleteById(@Min(1)@RequestParam("id") Long id) {
        shopService.deleteByShopId(id);
        return Result.success();
    }

    @Operation(summary = "根据商铺id来查询商铺")
    @GetMapping
    //注意这个param注解中要写方法参数的名字，因为我没有另外去搞所以反射获取不到参数名字
    public Result<ShopVO> getById(@Min(1)@RequestParam("id") Long id) {
        ShopVO shopVO = shopService.getByIdWithCache(id);
        return Result.success(shopVO);
    }

    @Operation(summary = "查询用户名下商铺")
    @GetMapping("/owner")
    public Result<List<ShopVO>> getOwnedShops() {
        List<ShopVO> shopVOS = shopService.getOwnedShops();
        return Result.success(shopVOS);
    }
}