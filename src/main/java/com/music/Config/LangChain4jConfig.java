package com.music.Config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import dev.langchain4j.data.message.ChatMessage;

@Configuration
public class LangChain4jConfig {

    @Bean
    public ChatMemoryStore chatMemoryStore() {
// ⭐ 这里的自定义实现非常稳，避开了官方库里的 NPE Bug
        return new ChatMemoryStore() {
            private final Map<Object, List<ChatMessage>> memories = new ConcurrentHashMap<>();

            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                return memories.getOrDefault(memoryId, new ArrayList<>());
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                memories.put(memoryId, messages);
            }

            @Override
            public void deleteMessages(Object memoryId) {
                memories.remove(memoryId);
            }
        };
    }



    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
// ⭐ 这个 Provider 会为每个用户自动创建/获取属于他们的账本
        return userId -> MessageWindowChatMemory.builder()
                .id(userId)
                .maxMessages(20) // 记住最近 20 条对话，避免 Token 消耗过大
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}