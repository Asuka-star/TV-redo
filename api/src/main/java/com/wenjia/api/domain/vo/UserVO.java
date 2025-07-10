package com.wenjia.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVO implements Serializable {
    //用户ID
    private Long id;
    //用户名
    private String username;
    //粉丝数
    private Integer fansNumber;
    //携带的jwt令牌
    private String token;
}
