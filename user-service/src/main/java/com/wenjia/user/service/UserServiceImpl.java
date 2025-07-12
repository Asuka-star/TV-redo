package com.wenjia.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenjia.api.domain.dto.UserDTO;
import com.wenjia.api.domain.po.User;
import com.wenjia.api.service.UserService;
import com.wenjia.common.context.BaseContext;
import com.wenjia.common.exception.UserException;
import com.wenjia.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

@DubboService
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(UserDTO userDTO) {
        //todo 这里要写sentinel限流
        /*//进行登录操作限流
        String key= RedisConstant.LOGIN_LIMIT_KEY+userDTO.getUsername();
        if(!JedisUtil.actionLimit(key,5,10)){
            throw new FollowException("请勿频繁操作");
        }*/
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        //通过用户名进行用户查找
        User user = lambdaQuery().eq(User::getUsername,username).one();
        if (user == null) {
            //没有找到用户
            throw new UserException("找不到用户名为" + username + "的用户");
        }
        //找到了用户，获得其中的密码，将password进行加密后与密码进行对比(注意顺序)
        //todo 这里改成spring security
        if (BCrypt.checkpw(password, user.getPassword())) {
            //todo rocketmq
            //发送登录的消息，然后进行拉取动态
            //postProducer.sendPostPullMessage(user.getId());
            //密码正确，返回登录的是哪一个用户
            return user;
        } else {
            //密码错误，抛出异常
            throw new UserException("密码错误");
        }
    }

    public Integer getFansNumberById(Integer id) {
        Long currentId = BaseContext.getCurrentId();
        if(currentId!=null&&!Objects.equals(id, currentId)) throw new UserException("用户信息异常");
        User user = getById(id);
        Integer fansNumberById = user.getFansNumber();
        if(fansNumberById==null) throw new UserException("当前用户不存在"+id);
        return fansNumberById;
    }

    @Override
    public void incrFansNumber(Long id) {
        lambdaUpdate().setSql("fans_number=fans_number+1").eq(User::getId,id).update();
    }

    @Override
    public void decrFansNumber(Long id) {
        lambdaUpdate().setSql("fans_number=fans_number-1").eq(User::getId,id).ge(User::getFansNumber,0).update();
    }

    @Override
    public void register(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        //先通过用户名进行用户查找
        User tempUser = lambdaQuery().eq(User::getUsername,username).one();
        if (tempUser != null) {
            //当前用户名已存在
            throw new UserException("当前用户名" + username + "已存在");
        }
        //将密码进行加密后再存储
        String passwordBCrypt = BCrypt.hashpw(password, BCrypt.gensalt());
        //将信息封装成对象
        User user = User.builder()
                .username(username)
                .password(passwordBCrypt)
                .build();
        save(user);
    }
}
