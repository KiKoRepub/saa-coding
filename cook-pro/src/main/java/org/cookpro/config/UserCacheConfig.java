package org.cookpro.config;

import jakarta.annotation.Resource;
import org.cookpro.config.properties.UserConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
用户缓存内容 配置类
 */
@Configuration
@EnableConfigurationProperties({UserConfigProperties.class})
public class UserCacheConfig {

    @Resource
    UserConfigProperties userConfigProperties;


}
