package com.wenjia.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wenjia.common.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;


public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //尝试除去userId
        BaseContext.removeCurrentId();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //尝试获取用户id
        String userInfo = request.getHeader("userId");
        if(StringUtils.isNotBlank(userInfo)){
            //存入到BaseContext中
            BaseContext.setCurrentId(Long.valueOf(userInfo));
        }
        //放行
        return true;
    }
}
