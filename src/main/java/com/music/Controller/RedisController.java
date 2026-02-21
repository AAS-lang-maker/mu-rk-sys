package com.music.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/test-redis")
    public String testRedis() {
        // 写入数据
        redisTemplate.opsForValue().set("testKey", "Hello Redis!");
        // 读取数据
        String value = redisTemplate.opsForValue().get("testKey");
        return "从 Redis 读取到: " + value;
    }
}