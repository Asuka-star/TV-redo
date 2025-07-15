package com.wenjia.gateway.config;



import com.wenjia.gateway.utils.MyRequestMethod;
import com.wenjia.gateway.utils.MyRequestPath;

import java.util.HashSet;
import java.util.Set;

/**
 * 用来封装不用拦截的请求路径
 */
public class RequestPathConfig {
    //用来存放可以直接放行的请求路径
    public static Set<MyRequestPath> requiredRequestPathSet = new HashSet<>();
    //用来存放可能携带token的请求路径
    public static Set<MyRequestPath> mayRequiredRequestPathSet = new HashSet<>();

    static {
        //登录请求
        requiredRequestPathSet.add(new MyRequestPath("/user/login", MyRequestMethod.POST));
        //注册请求
        requiredRequestPathSet.add(new MyRequestPath("/user", MyRequestMethod.POST));
        //通过shopId来查询商铺请求
        requiredRequestPathSet.add(new MyRequestPath("/shop", MyRequestMethod.GET));
        //通过shopId来查询优惠券请求
        requiredRequestPathSet.add(new MyRequestPath("/coupon", MyRequestMethod.GET));
        //查询缓存中优惠券的库存
        requiredRequestPathSet.add(new MyRequestPath("/coupon/stock", MyRequestMethod.GET));

        //评论的分页查询请求
        mayRequiredRequestPathSet.add(new MyRequestPath("/comment/page", MyRequestMethod.GET));
        //商铺的分页查询请求
        mayRequiredRequestPathSet.add(new MyRequestPath("/shop/page", MyRequestMethod.GET));
    }
}
