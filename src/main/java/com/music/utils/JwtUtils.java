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
 * JWT工具类：生成Token、解析Token、验证Token
 * 复制粘贴，无需手动敲写
 */
@Component
public class JwtUtils {
    // 密钥（至少256位，随便写一串复杂的字符串）
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("vote_system_2026_jwt_secret_key_1234567890".getBytes());
    // Token过期时间：2小时（单位：毫秒）
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000;

    /**
     * 生成Token：封装用户ID和用户名
     * @param userId 用户ID
     * @param username 用户名
     * @return Token字符串
     */
    public  static String generateToken(int userId, String username) {
        // 封装Token的自定义数据（Claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims) // 设置自定义数据
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 设置过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 设置签名算法和密钥
                .compact();
    }

    /**
     * 解析Token：获取其中的用户信息
     * @param token Token字符串
     * @return 封装了用户信息的Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 设置密钥
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效（是否过期、是否被篡改）
     * @param token Token字符串
     * @return true：有效，false：无效
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // 解析失败，说明Token无效（过期、被篡改、格式错误）
            return false;
        }
    }
}
