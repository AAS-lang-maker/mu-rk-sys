package com.music.Service.impl;

import com.music.Service.AiAuditService;
import com.music.pojo.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;@Slf4j
@Service
public class AiAuditServiceImpl implements AiAuditService {

    @Value("${siliconflow.api-key}")
    private String apiKey;

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String MODEL = "deepseek-ai/DeepSeek-V3.2";

    // 本地敏感词兜底（就算AI没调用，脏话也100%拦截）
    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "傻逼", "煞笔", "去死", "垃圾", "废物", "操你", "妈的"
    );

    @Override
    public int getScore(Comment comment) {
        log.info("🤖 AI审核启动，评论ID：{}", comment.getComId());

        // ✅ 关键：用正确的getter拿内容！
        String content = getRealContent(comment);
        log.info("📝 拿到评论内容：{}", content);

        if (content == null || content.isBlank()) {
            log.warn("⚠️ 评论内容为空，返回0分");
            return 0;
        }

        // 🔥 第一步：本地敏感词拦截（脏话直接95分，必拦！）
        for (String word : SENSITIVE_WORDS) {
            if (content.contains(word)) {
                log.warn("🚨 敏感词命中！内容：{} → 95分", content);
                return 95;
            }
        }

        // 🔥 第二步：调用AI打分
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("temperature", 0.0);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", "你是评论审核员，只返回0-100的数字，辱骂脏话必须打80分以上，正常内容打10-30分。内容：" + content);
            messages.add(msg);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

            // 解析AI返回
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            String scoreStr = ((Map<String, String>) choices.get(0).get("message")).get("content").trim();

            int score = Integer.parseInt(scoreStr);
            score = Math.max(0, Math.min(score, 100)); // 限制0-100
            log.info("✅ AI打分完成，内容：{} → {}分", content, score);
            return score;

        } catch (Exception e) {
            log.error("❌ AI调用异常，使用兜底分60", e);
            return 60;
        }
    }
    // 强制、暴力、100%拿到评论内容
    private String getRealContent(Comment comment) {
        try {
            // 先试你现有的
            if (comment.getComContent() != null) {
                return comment.getComContent();
            }

            // 🔥 关键：如果为null，直接反射拿 comment_content 原始值
            java.lang.reflect.Field field = Comment.class.getDeclaredField("commentContent");
            field.setAccessible(true);
            return (String) field.get(comment);
        } catch (Exception e) {
            return "";
        }
    }
}