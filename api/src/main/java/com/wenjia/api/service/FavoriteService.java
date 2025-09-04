package com.wenjia.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.po.Favorite;

public interface FavoriteService extends IService<Favorite> {
    //新增收藏
    void favorite(Long postId);

    //取消收藏
    void cancelFavorite(Long postId);

    //判断是否收藏过
    boolean hasFavorite(Long userId,Long postId);
}
