package com.wenjia.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.ThumbDTO;
import com.wenjia.api.domain.pageQuery.ThumbPageQuery;
import com.wenjia.api.domain.po.Thumb;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.domain.vo.ThumbVO;

import java.util.List;

public interface ThumbService extends IService<Thumb> {
    //新增点赞
    void thumb(ThumbDTO thumbDTO);

    //取消点赞
    void cancelThumb(ThumbDTO thumbDTO);

    //判断是否点过赞
    boolean hasThumb(Integer type,Long userId,Long targetId);

    //分页查询点赞
    PageResult<ThumbVO> pageQuery(ThumbPageQuery thumbPageQuery);

    //查询用户点赞的所有商铺id
    List<Long> thumbWithShopIds(Long userId);

    //查询用户点赞的所有评论id
    List<Long> thumbWithCommentIds(Long userId);

    //删除商铺下的所有点赞
    void deleteByShopId(Long shopId);
}
