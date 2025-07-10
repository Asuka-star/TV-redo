package com.wenjia.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
//对于下面这个注解，要么有其他地方使用@EnableConfigurationProperties进行引用
// 或者使用@Component来将这个类放入spring中管理，不然就会报错
@ConfigurationProperties(prefix = "wenjia.jwt")
public class JwtConfig {
    private String secretKey;
    private Long duration;
}
