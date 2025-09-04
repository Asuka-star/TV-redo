package com.wenjia.post.controller;


import com.wenjia.api.domain.dto.PostDTO;
import com.wenjia.api.domain.po.Post;
import com.wenjia.api.domain.vo.PostVO;
import com.wenjia.api.domain.vo.ScrollResult;
import com.wenjia.api.service.PostService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@Tag(name = "动态接口")
public class PostController {
    @DubboReference
    private PostService postService;

    @Operation(summary = "新增动态")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody PostDTO postDTO){
        postService.add(postDTO);
        return Result.success();
    }

    @Operation(summary = "游标查询动态")
    @GetMapping("/page")
    public Result<ScrollResult<Post>> queryPage(@RequestParam("cursor") Long cursor,
                                                @RequestParam("offset") Integer offset){
        //todo 这里会一直查下去，有问题
        // 习惯着养成一个良好的git提交习惯
        ScrollResult<Post> result=postService.queryPage(cursor,offset);
        return Result.success(result);
    }

    @Operation(summary = "查询单个动态")
    @GetMapping
    public Result<PostVO> getById(@RequestParam("postId") Long postId){
        PostVO postVO=postService.getById(postId);
        return Result.success(postVO);
    }
}