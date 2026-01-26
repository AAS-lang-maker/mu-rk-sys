package com.music.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类，解决前后端端口/域名不一致的请求问题
 */
@Configuration // 标记为Spring配置类，启动时自动加载
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有接口生效
                .allowedOriginPatterns("*") // 允许所有前端域名/端口
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方式
                .allowCredentials(true) // 允许携带Cookie
                .maxAge(3600); // 跨域请求有效期（秒）
    }
}
