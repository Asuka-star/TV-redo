package com.wenjia.gateway.route;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {
    private final RouteDefinitionWriter writer;
    private final NacosConfigManager nacosConfigManager;

    private final String dataId="gateway-routes.json";
    private final String group="DEFAULT_GROUP";

    private final Set<String>routeIds=new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException{
        //注册监听器并且首次拉取配置
        String s = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String s) {
                        updateConfigInfo(s);
                    }
                });
        updateConfigInfo(s);
    }

    private void updateConfigInfo(String configInfo) {
        log.debug("监听到路由配置变更,{}",configInfo);
        for(String id:routeIds){
            writer.delete(Mono.just(id)).subscribe();
        }
        routeIds.clear();
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        if(CollUtil.isEmpty(routeDefinitions))return;
        for(RouteDefinition routeDefinition:routeDefinitions){
            writer.save(Mono.just(routeDefinition)).subscribe();
            routeIds.add(routeDefinition.getId());
        }
    }
}
