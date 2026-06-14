package com.music.Service.impl;

import com.music.Service.*;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class MusicAgentServiceImpl implements MusicAgentService {
    @Autowired
    private AiChatLogService aiChatLogService;
    @Autowired
    private MusicAgent assistant;
    @Autowired
    private ChatMemoryStore chatMemoryStore;
    private String FORMAT= """
            你是一个专业得音乐推荐大管家。\\n" + "请结合用户对音乐榜单" +
                            "的喜好，喜欢的音乐风格和历史聊天记录，为用户提供精准的音乐建议。\\n" +
                            "如果需要获取实时榜单，你可以调用相关工具
            """;
    @Override
    public TokenStream chat(int userId, String userMessage) {
        log.info("已经存入记忆，准备调用Agent,即将生成流式回复");
        String systemPrompt = String.format(FORMAT);

// 1. 获取流（这一行保持不变）
        TokenStream tokenStream = assistant.chat(userId, systemPrompt, userMessage);

        tokenStream.onNext(log::debug)
                .onComplete(response -> {
                    String aiReply = response.content().text();
                    aiChatLogService.saveAiResponse(userId, aiReply);
                })
                .onError(error ->log.error("AI对话出错", error))
                .start(); // 别忘了最后这个启动开关

        return tokenStream;
    }
}
