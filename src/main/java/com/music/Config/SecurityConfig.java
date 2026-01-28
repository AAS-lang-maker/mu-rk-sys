package com.music.Config; // 替换成你自己的包名

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security 配置类，主要用于声明密码加密器的Bean
 */
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
}