package com.wenjia.gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用来封装不用拦截的请求路径
 */
@Data
@Component
@ConfigurationProperties(prefix = "wenjia.auth")
public class RequestPathConfig {
    //用来存放可以直接放行的请求路径
    private List<String> excludePaths;
    //用来存放可能含有token的请求路径
    private List<String> mayPaths;
}
