package com.wenjia.thumb.controller;


import com.wenjia.api.domain.dto.ThumbDTO;
import com.wenjia.api.domain.pageQuery.ThumbPageQuery;
import com.wenjia.api.domain.vo.PageResult;
import com.wenjia.api.domain.vo.ThumbVO;
import com.wenjia.api.service.ThumbService;
import com.wenjia.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/thumb")
@Tag(name = "点赞接口")
public class ThumbController {
    @DubboReference
    private ThumbService thumbService;

    @Operation(summary = "新增点赞记录")
    @PostMapping
    public Result<Void> thumb(@Valid @RequestBody ThumbDTO thumbDTO) {
        thumbService.thumb(thumbDTO);
        return Result.success();
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping
    public Result<Void> cancelThumb(@Valid ThumbDTO thumbDTO) {
        thumbService.cancelThumb(thumbDTO);
        return Result.success();
    }

    //todo 后面还有每个服务的熔断降级
    @Operation(summary = "分页查询用户点赞记录")
    @GetMapping("/page")
    public Result<PageResult<ThumbVO>> page(@Valid ThumbPageQuery thumbPageQuery) {
        PageResult<ThumbVO> thumbVOPageResult = thumbService.pageQuery(thumbPageQuery);
        //返回值
        return Result.success(thumbVOPageResult);
    }
}
