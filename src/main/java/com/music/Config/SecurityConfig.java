/*

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
/*@EnableWebSecurity
@Configuration // 告诉Spring这是一个配置类
public class SecurityConfig {

    /**
     * 声明BCryptPasswordEncoder为Spring Bean，让整个项目都能注入使用
     */
  /**  @Bean // 关键注解：把这个方法返回的对象交给Spring容器管理
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
                        .requestMatchers("/api/**").permitAll()

                        // --- 👇 新增这一行：放行 WebSocket 握手请求 ---
                        // 注意：这里必须写你的 endpoint 路径
                        .requestMatchers("/ws-endpoint").permitAll()
                        // 3. 其他所有请求都需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
*/

package com.music.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置类
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器 Bean
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 跨域配置 Bean (前后端分离必须)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源（上线建议改为具体前端域名）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许所有请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许携带 Cookie/凭证
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/css/**",
                                "/js/**",
                                "/player/**",
                                "/images/**"
                        ).permitAll()

                        .requestMatchers("/api/**").permitAll()

                        .requestMatchers("/AiComment/**").permitAll()

                        .requestMatchers("/ws-endpoint/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        .requestMatchers("/play/**").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}