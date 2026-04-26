package com.music.Config;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        // 告诉 Spring：我们需要一个在内存里存聊天记录的仓库
        return new InMemoryChatMemoryStore();
    }
}