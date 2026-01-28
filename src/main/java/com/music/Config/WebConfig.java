package com.music.Config;

import com.music.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类（一级包：config）
 * 注册拦截器，配置拦截/放行规则
 */
@Configuration // 标记为配置类，Spring启动时自动加载
public class WebConfig implements WebMvcConfigurer {

    // 注入自定义的登录拦截器
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 注册拦截器的核心方法
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器
        registry.addInterceptor(loginInterceptor)
                // 拦截所有请求（/** 表示匹配所有层级的接口）
                .addPathPatterns("/**")
                // 放行的接口（无需登录即可访问）
                .excludePathPatterns(
                        "/user/register", // 注册接口
                        "/user/login",    // 登录接口
                        // 放行静态资源（如果有前端页面，比如html/css/js，需要放行）
                        "/**.html",
                        "/**.css",
                        "/**.js",
                        "/images/**"
                );
    }
}
