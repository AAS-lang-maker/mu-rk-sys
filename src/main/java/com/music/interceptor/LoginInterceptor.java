package com.music.interceptor;

import com.music.utils.JwtUtils;
import com.music.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头中获取Token（前端需将Token放在header的token字段中）
        String token = request.getHeader("token");

        // 2. 验证Token：为空 或 无效，则拦截
        if (token == null || !jwtUtil.isTokenValid(token)) {
            // 设置响应格式为JSON，解决中文乱码
            response.setContentType("application/json;charset=utf-8");
            // 构建未登录的返回结果
            Result<String> result = Result.error("请先登录！");
            // 将Result对象转为JSON字符串返回给前端
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResult = objectMapper.writeValueAsString(result);
            // 输出JSON结果
            PrintWriter writer = response.getWriter();
            writer.write(jsonResult);
            writer.flush();
            writer.close();
            // 拦截请求，不继续执行
            return false;
        }

        // 3. Token有效，放行请求
        return true;
    }
}

