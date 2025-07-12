package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.FollowDTO;
import com.wenjia.api.domain.pageQuery.FollowPageQuery;
import com.wenjia.api.domain.po.Follow;
import com.wenjia.api.domain.vo.FollowVO;
import com.wenjia.api.domain.vo.PageResult;

import java.util.List;

public interface FollowService extends IService<Follow> {
    //取消关注
    void cancelFollow(FollowDTO followDTO);

    //新增关注
    void add(FollowDTO followDTO);

    //分页查询关注
    PageResult<FollowVO> pageQuery(FollowPageQuery followPageQuery);

    //判断是否关注过
    boolean hasFollow(Integer type,Long userId,Long targetId);

    //查询用户关注的所有商铺id
    List<Long> followWithShopIds(Long userId);

    //查询用户关注的所有用户id
    List<Long> followWithUserIds(Long userId);

    //查询用户关注的所有商铺和用户的id
    List<Long> getByUserId(Long userId);

    //删除商铺下的所有关注
    void deleteByShopId(Long shopId);
}
