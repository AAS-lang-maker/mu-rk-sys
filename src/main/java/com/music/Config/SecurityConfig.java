package com.music.Config; // 替换成你自己的包名

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security 配置类，主要用于声明密码加密器的Bean
 */
@EnableWebSecurity
@Configuration // 告诉Spring这是一个配置类
public class SecurityConfig {

    /**
     * 声明BCryptPasswordEncoder为Spring Bean，让整个项目都能注入使用
     */
    @Bean // 关键注解：把这个方法返回的对象交给Spring容器管理
    public BCryptPasswordEncoder passwordEncoder() {
        // 创建BCryptPasswordEncoder实例并返回
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF，对于无状态的 REST API 和 Swagger UI 通常是安全的
                .csrf(csrf -> csrf.disable())
                // 2. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行 Swagger UI 和 API 文档相关的所有路径
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources",
                                "/swagger-resources/**"
                        ).permitAll()
                        // 放行你的 API 接口路径，例如 /music/**
                        .requestMatchers("/music/**").permitAll()
                        // 3. 其他所有请求都需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
