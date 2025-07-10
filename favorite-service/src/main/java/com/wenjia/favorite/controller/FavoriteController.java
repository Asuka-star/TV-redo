
package com.wenjia.favorite.controller;


import com.wenjia.api.domain.dto.FavoriteDTO;
import com.wenjia.api.service.FavoriteService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @DubboReference
    private FavoriteService favoriteService;


    @Operation(summary = "新增收藏")
    @PostMapping
    public Result<Void> favorite(@RequestBody FavoriteDTO favoriteDTO) {
        favoriteService.favorite(favoriteDTO.getPostId());
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping
    public Result<Void> cancelFavorite(FavoriteDTO favoriteDTO) {
        favoriteService.cancelFavorite(favoriteDTO.getPostId());
        return Result.success();
    }
}
