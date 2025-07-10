package com.wenjia.api.domain.dto;


import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 接收用户名和密码两个参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    //用户名
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{6,12}$",message = "用户名不能包括特殊符号,并且长度为6-12位")
    private String username;
    //密码
    @Pattern(regexp = "[a-zA-Z0-9]{8,16}",message = "密码只能包括大小写字母和数字,并且长度在8-16位")
    private String password;
}
