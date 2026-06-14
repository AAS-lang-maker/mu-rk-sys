package com.music.interceptor;

import com.music.utils.JwtUtils;
import com.music.utils.Result;
import com.music.utils.ThreadLocalUtil; // 记得导入你的托盘工具类
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录拦截器：全能扫描版
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 开始多渠道搜寻 Token
        String token = request.getHeader("Authorization"); // 渠道A：标准请求头

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        if (token == null || token.isEmpty()) {
            token = request.getHeader("token"); // 渠道B：自定义请求头
        }

        if (token == null || token.isEmpty()) {
            token = request.getParameter("token"); // 渠道C：URL参数 (?token=xxx)
        }

        // 2. 验证 Token 是否有效
        if (token == null || !JwtUtils.isTokenValid(token)) {
            // 验证失败：拦截并返回 JSON
            response.setContentType("application/json;charset=utf-8");
            Result<String> result = Result.error("请先登录！");
            String jsonResult = new ObjectMapper().writeValueAsString(result);
            response.getWriter().write(jsonResult);
            return false;
        }

        // 3. 【核心操作】：验证通过，把用户 ID 存进 ThreadLocal 托盘
        try {
            Integer userId = JwtUtils.getUserIdFromToken(token);
            ThreadLocalUtil.set(userId);
            return true; // 放行！
        } catch (Exception e) {
            // 解析失败也算无效
            return false;
        }
    }

    /**
     * 请求处理完后，一定要把托盘刷干净！
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
        ThreadLocalUtil.remove();
    }
}