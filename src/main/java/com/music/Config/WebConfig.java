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
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(    // 以下是“白名单”，不用登录也能访问
                        "/api/user/register", // 你的注册接口（根据实际路径检查一下）
                        "/api/user/login",    // 你的登录接口
                        "/api/role/register",
                        "/api/role/login",
                        "/js/**",         // 放行 JS 文件夹
                        "/player/**",     // 放行播放器插件文件夹
                        "/login.html",    // 放行登录页
                        "/register.html", // 放行注册页
                        "/index.html",    // 放行首页
                        "/*.css",         // 放行根目录下的 CSS
                        "/*.js",           // 放行根目录下的 JS
                        // 下面这一行是关键，放行 Swagger 的接口数据
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }

}