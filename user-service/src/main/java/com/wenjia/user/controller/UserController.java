package com.wenjia.user.controller;


import com.wenjia.api.domain.dto.UserDTO;
import com.wenjia.api.domain.po.User;
import com.wenjia.api.domain.vo.UserVO;
import com.wenjia.api.service.UserService;
import com.wenjia.user.config.JwtConfig;
import com.wenjia.common.result.Result;
import com.wenjia.user.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {
    @DubboReference
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping(value = "/login")
    public Result<UserVO> login(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.login(userDTO);
        //登录成功后，需要给予jwt令牌
        //封装用户的id
        Map<String, Object> claim = new HashMap<>();
        claim.put("userId", user.getId());
        //获取jwt令牌
        String jwt = JwtUtil.createJwt(JwtConfig.secretKey, JwtConfig.duration, claim);
        //返回前端userVO
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .token(jwt)
                .username(user.getUsername())
                .build();
        return Result.success(userVO);
    }

    @Operation(summary = "用户注册")
    @PostMapping
    public Result<Void> register(@Valid @RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return Result.success();
    }

    @Operation(summary = "查询用户粉丝数")
    @GetMapping("/fans")
    public Result<Integer> getFansNumber(@Min(1)@RequestParam Integer id){
        Integer fansNumber= userService.getFansNumberById(id);
        return Result.success(fansNumber);
    }
}
