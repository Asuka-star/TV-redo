package com.wenjia.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wenjia.api.domain.dto.UserDTO;
import com.wenjia.api.domain.po.User;

public interface UserService extends IService<User> {
    //登录功能
    User login(UserDTO userDTO);

    //注册功能
    void register(UserDTO userDTO);

    //查询粉丝数量
    Integer getFansNumberById(Long id);

    //增加粉丝数
    void incrFansNumber(Long id);

    //减少粉丝数
    void decrFansNumber(Long id);
}
