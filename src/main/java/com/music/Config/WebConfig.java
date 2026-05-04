package com.music.Config;

import com.music.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：注册拦截器，配置拦截/放行规则
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 注入你的登录拦截器
    @Autowired
    private LoginInterceptor loginInterceptor;

    // 放行静态资源：让 Spring Boot 能找到 static 下的 HTML/CSS/JS
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 注册拦截器的核心方法
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 2. 注册登录拦截器并配置规则
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")   // 只拦截接口
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/role/login",
                        "/api/role/register",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }

}