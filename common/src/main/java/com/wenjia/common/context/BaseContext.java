package com.wenjia.common.context;

public class BaseContext {
    //利用ThreadLocal来存放当前登录用户的id
    static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //获得当前id
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    //设置当前id
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    //移除当前id
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
