package com.wenjia.follow.controller;


import com.wenjia.api.domain.dto.FollowDTO;
import com.wenjia.api.domain.pageQuery.FollowPageQuery;
import com.wenjia.api.domain.vo.FollowVO;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.service.FollowService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
@Tag(name = "关注接口")
public class FollowController {

    @DubboReference
    private FollowService followService;

    @Operation(summary = "新增关注")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody FollowDTO followDTO) {
        followService.add(followDTO);
        return Result.success();
    }

    @Operation(summary = "取消关注")
    @DeleteMapping
    public Result<Void> cancelFollow(@Valid FollowDTO followDTO) {
        followService.cancelFollow(followDTO);
        return Result.success();
    }

    @Operation(summary = "分页查询关注")
    @GetMapping("/page")
    public Result<PageResult<FollowVO>> page(@Valid FollowPageQuery followPageQuery) {
        PageResult<FollowVO> followVOPageResult = followService.pageQuery(followPageQuery);
        //返回值
        return Result.success(followVOPageResult);
    }
}
