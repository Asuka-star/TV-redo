package com.wenjia.comment.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.CommentDTO;
import com.wenjia.api.domain.pageQuery.CommentPageQuery;
import com.wenjia.api.domain.po.Comment;
import com.wenjia.api.domain.po.Thumb;
import com.wenjia.api.domain.vo.CommentVO;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.service.CommentService;
import com.wenjia.api.service.FollowService;
import com.wenjia.api.service.ThumbService;
import com.wenjia.comment.mapper.CommentMapper;
import com.wenjia.common.constant.RedisConstant;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.CommentException;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@DubboService
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper,Comment> implements CommentService {

    @DubboReference
    private ThumbService thumbService;
    @DubboReference
    private FollowService followService;

    private final RedisTemplate<String,Objects> redisTemplate;

    @Override
    public PageResult<CommentVO> pageQuery(CommentPageQuery commentPageQuery) {
        Integer type = commentPageQuery.getType();
        if(!(type==0||type==1)) throw new CommentException("没有当前类型的目标");
        //查询当前页的数据
        Page<Comment> page = lambdaQuery().eq(Comment::getType, commentPageQuery.getType())
                .eq(Comment::getTargetId, commentPageQuery.getTargetId())
                .page(commentPageQuery.toMpPage("id", true));
        //将comment转化成commentVO
        List<CommentVO> commentVOList = null;
        //todo 这里一样会有信息泄漏问题
        if (commentPageQuery.getUserId() == null) {
            //当前没有用户登录
            commentVOList=page.getRecords().stream()
                    .map(comment-> {
                        CommentVO commentVO=BeanUtil.copyProperties(comment, CommentVO.class);
                        commentVO.setHasFollow(false);
                        commentVO.setHasThumb(false);
                        return commentVO;
                    })
                    .toList();
        } else {
            //当前有用户登录，判断用户是否已经关注和点赞
            Long pageQueryUserId = commentPageQuery.getUserId();
            //查询用户点赞的所有评论id
            List<Long> thumbWithCommentIds = thumbService.lambdaQuery()
                    .eq(Thumb::getType,1)
                    .eq(Thumb::getUserId,pageQueryUserId)
                    .select(Thumb::getTargetId)
                    .list()
                    .stream().map(Thumb::getTargetId).toList();
            //查询用户关注的所有用户id
            List<Long> followWithUserIds = followService.followWithUserIds(pageQueryUserId);
            //转化
            commentVOList=page.getRecords().stream()
                    .map(comment-> {
                        CommentVO commentVO=BeanUtil.copyProperties(comment, CommentVO.class);
                        commentVO.setHasFollow(thumbWithCommentIds.contains(comment.getId()));
                        commentVO.setHasThumb(followWithUserIds.contains(comment.getId()));
                        return commentVO;
                    })
                    .toList();
        }
        //封装返回结果
        PageResult<CommentVO> commentPageResult = new PageResult<>();
        commentPageResult.setTotal(page.getTotal());
        commentPageResult.setRecords(commentVOList);
        return commentPageResult;
    }

    @Override
    public void add(CommentDTO commentDTO) {
        Integer type = commentDTO.getType();
        if(!(type==0||type==1)) throw new CommentException("没有当前类型的目标");
        //先将CommentDTO对象转化成Comment对象
        Comment comment = BeanUtil.copyProperties(commentDTO,Comment.class);
        //删除缓存
        //todo 这里一样要开启事务，给目标对象增加点赞数
        save(comment);
        if(comment.getType()==0) redisTemplate.delete(RedisConstant.SHOP_KEY+comment.getTargetId());
        if(comment.getType()==1) redisTemplate.opsForHash().increment(RedisConstant.POST_KEY+comment.getTargetId(),"commentNumber",1L);
    }

    @Override
    public void deleteById(Long id) {
        //当然也可以前端来判断当前用户是否能删，但是后台也写可以防止postman那些方式的请求
        //先判断当前用户是否是发布这个评论的用户
        Comment comment = getById(id);
        if(comment==null) throw new CommentException("评论不存在");
        if (!Objects.equals(comment.getUserId(), BaseContext.getCurrentId())) {
            //当前用户不是发布者，不能删除该评论，抛出异常
            throw new CommentException("您不是此评论的发布者，无法删除此评论");
        }
        //todo 这里一样要开启事务，给目标对象减少点赞数，不只是是不是写成db来防止循环注入
        lambdaUpdate().eq(Comment::getId,id).remove();
        if(comment.getType()==0) redisTemplate.delete(RedisConstant.SHOP_KEY+comment.getTargetId());
        if(comment.getType()==1) redisTemplate.opsForHash().increment(RedisConstant.POST_KEY+comment.getTargetId(),"commentNumber",-1L);
    }
}
