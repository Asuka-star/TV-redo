package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.CommentDTO;
import com.wenjia.api.domain.pageQuery.CommentPageQuery;
import com.wenjia.api.domain.po.Comment;
import com.wenjia.api.domain.vo.CommentVO;
import com.wenjia.api.domain.vo.PageResult;

public interface CommentService extends IService<Comment> {
    //分页查询评论
    PageResult<CommentVO> pageQuery(CommentPageQuery commentPageQuery);

    //新增评论
    void add(CommentDTO commentDTO);

    //根据id删除评论
    void deleteById(Long id);
}
