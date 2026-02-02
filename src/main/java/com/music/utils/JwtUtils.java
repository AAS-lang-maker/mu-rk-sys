package com.music.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类：生成Token、解析Token、验证Token、从Token获取用户ID
 */
@Component
public class JwtUtils {
    // 密钥（已满足256位要求，复用这个SECRET_KEY即可，不要重复定义）
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("vote_system_2026_jwt_secret_key_1234567890".getBytes());
    // Token过期时间：2小时（单位：毫秒）
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000;

    /**
     * 生成Token：封装用户ID和用户名（已正确把userId存入Payload，无需修改）
     * @param userId 用户ID
     * @param username 用户名
     * @return Token字符串
     */
    public static String generateToken(int userId, String username) {
        // 封装Token的自定义数据（Payload区域，解析时能获取）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims) // 设置自定义数据（userId/username存在这里）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 设置过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 签名（和解析用同一个密钥）
                .compact();
    }

    /**
     * 从Token中解析出用户ID（核心修改：复用SECRET_KEY，调用现有parseToken方法，避免重复代码）
     * @param token JWT令牌（需去掉"Bearer "前缀后传入）
     * @return 用户ID
     * @throws Exception 解析失败（token无效、过期、无userId）
     */
    public static Integer getUserIdFromToken(String token) throws Exception {
        // 调用已有的解析方法，获取Token中的所有数据（Payload）
        Claims claims = parseToken(token);
        // 从Payload中取userId（和generateToken里的key"userId"完全对应）
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new Exception("Token中未包含用户ID");
        }
        // 转换为Integer返回（适配你的业务）
        return Integer.parseInt(userIdObj.toString());
    }

    /**
     * 解析Token：获取其中的所有用户信息（私有化，内部复用，也可改为public按需使用）
     * @param token Token字符串（需去掉"Bearer "前缀）
     * @return 封装了用户信息的Claims（Payload数据）
     */
    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 用全局的SECRET_KEY，解决爆红核心问题
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效（是否过期、是否被篡改）
     * @param token Token字符串（需去掉"Bearer "前缀）
     * @return true：有效，false：无效
     */
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查Token是否过期：过期时间在当前时间之后，即为有效
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // 解析失败=Token无效（过期、被篡改、格式错误、密钥不匹配）
            return false;
        }
    }
}