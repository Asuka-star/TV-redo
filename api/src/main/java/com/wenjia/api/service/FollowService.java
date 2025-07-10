package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.FollowDTO;
import com.wenjia.api.domain.pageQuery.FollowPageQuery;
import com.wenjia.api.domain.po.Follow;
import com.wenjia.api.domain.vo.FollowVO;
import com.wenjia.api.domain.vo.PageResult;

public interface FollowService extends IService<Follow> {
    //取消关注
    void cancelFollow(FollowDTO followDTO);

    //新增关注
    void add(FollowDTO followDTO);

    //分页查询关注
    PageResult<FollowVO> pageQuery(FollowPageQuery followPageQuery);

    //判断是否关注过
    boolean hasFollow(Integer type,Long userId,Long targetId);
}
