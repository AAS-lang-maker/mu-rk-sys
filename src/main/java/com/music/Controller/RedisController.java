package com.music.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Redis测试", description = "Redis测试相关接口")
public class RedisController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/test-redis")
    @Operation(summary = "测试Redis", description = "测试Redis的读写操作")
    public String testRedis() {
        // 写入数据
        redisTemplate.opsForValue().set("testKey", "Hello Redis!");
        // 读取数据
        String value = redisTemplate.opsForValue().get("testKey");
        return "从 Redis 读取到: " + value;
    }
}