package com.music.Service;


import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface MusicAgent {
    @SystemMessage("{{systemPrompt}}") // 1. 定义一个模板变量
    TokenStream chat(
            @MemoryId int userId,
            @V("systemPrompt") String systemPrompt, // 2. 用 @V 把参数填进模板
            @UserMessage String userMessage
    );
}