package com.music.interceptor;

import com.music.Exception.ServiceException;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
// 新的（正确）
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 登录拦截器（一级包：interceptor）
 * 验证JWT Token有效性，拦截未登录请求
 */
@Component // 交给Spring容器管理
public class LoginInterceptor implements HandlerInterceptor {

    // 注入JWT工具类
    @Autowired
    private JwtUtils jwtUtil;

    /**
     * 请求处理前执行：核心的Token验证逻辑
     * @return true：放行，false：拦截
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServiceException {
// 1. 还是从请求头拿 Token
        String token = request.getHeader("token");

// 2. 核心变化！如果不合法，直接抛异常，别自己在那写 JSON 了
        if (token == null || !jwtUtil.isTokenValid(token)) {
// 【关键】直接抛出我们刚才建的异常信号
            throw new  ServiceException("请先登录！");
        }

// 3. 正常放行
        return true;
    }
}

