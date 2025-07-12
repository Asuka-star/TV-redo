package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.PostDTO;
import com.wenjia.api.domain.po.Post;
import com.wenjia.api.domain.vo.PostVO;
import com.wenjia.api.domain.vo.ScrollResult;

public interface PostService extends IService<Post> {
    //新增用户
    void add(PostDTO postDTO);
    //实现分页滚动查询
    ScrollResult<Post> queryPage(Long cursor, Integer offset);
    //根据id查询post
    PostVO getById(Long postId);
    //增加帖子评论数
    void incrCommentNumber(Long postId);
    //减少帖子评论数
    void decrCommentNumber(Long postId);
    //增加帖子点赞数
    void incrThumbNumber(Long id);
    //减少帖子点赞数
    void decrThumbNumber(Long id);
}
