package com.wenjia.comment.controller;


import com.wenjia.api.domain.dto.CommentDTO;
import com.wenjia.api.domain.pageQuery.CommentPageQuery;
import com.wenjia.api.domain.vo.CommentVO;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.service.CommentService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "评论接口")
@RequestMapping("/comment")
public class CommentController {

    @DubboReference
    private CommentService commentService;

    @GetMapping("/page")
    @Operation(summary = "分页查询评论")
    public Result<PageResult<CommentVO>> page(@Valid CommentPageQuery commentPageQuery) {
        PageResult<CommentVO> commentPageResult = commentService.pageQuery(commentPageQuery);
        return Result.success(commentPageResult);
    }

    @Operation(summary = "新增评论")
    @PostMapping
    public Result<Void> add(@RequestBody @Valid CommentDTO commentDTO) {
        commentService.add(commentDTO);
        return Result.success();
    }

    @Operation(summary = "根据评论id来删除评论")
    @DeleteMapping
    public Result<Void> deleteById(@RequestParam("id") Long id) {
        commentService.deleteById(id);
        return Result.success();
    }
}
