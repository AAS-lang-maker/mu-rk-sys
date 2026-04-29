package com.music.Service;

import dev.langchain4j.service.TokenStream;

public interface MusicAgentService {
    TokenStream chat(int userId, String userMessage);
}
