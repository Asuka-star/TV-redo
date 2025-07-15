package com.wenjia.gateway.filter;

import com.wenjia.gateway.config.JwtConfig;
import com.wenjia.gateway.config.RequestPathConfig;
import com.wenjia.gateway.utils.JwtUtil;
import com.wenjia.gateway.utils.MyRequestMethod;
import com.wenjia.gateway.utils.MyRequestPath;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtConfig.class)
public class AuthGlobalFilter implements GlobalFilter, Order {

    private final JwtConfig jwtConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        String method = request.getMethod().toString();
        if(isExclude(path.toString(),method)){
            //不需要进行拦截
            return chain.filter(exchange);
        }
        //获取请求头
        String token=null;
        List<String> headers = request.getHeaders().get("token");
        if(!CollectionUtils.isEmpty(headers)){
            token=headers.get(0);
        }
        //校验并解析token
        Long userId=null;
        try {
            Claims claims = JwtUtil.parseJWT(jwtConfig.getSecretKey(), token);
            userId= Long.parseLong(claims.get("userId").toString());
        } catch (RuntimeException e) {
            //解析token失败，再来判断一下当前路径是可以不用传递token的
            if(isMay(path.toString(),method)){
                return chain.filter(exchange);
            }
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        //放入请求头中
        String userInfo=userId.toString();
        ServerWebExchange serverWebExchange = exchange.mutate()
                .request(builder -> builder.header("userId", userInfo))
                .build();
        //放行
        return chain.filter(serverWebExchange);
    }

    private boolean isMay(String path,String method) {
        return RequestPathConfig.mayRequiredRequestPathSet
                .contains(new MyRequestPath(path, MyRequestMethod.valueOf(method)));
    }

    private boolean isExclude(String path,String method) {
        return RequestPathConfig.requiredRequestPathSet
                .contains(new MyRequestPath(path, MyRequestMethod.valueOf(method)));
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}