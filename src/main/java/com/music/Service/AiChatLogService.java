package com.music.Service;

import com.music.pojo.AiChatLog;

import java.util.List;

public interface AiChatLogService {
    List<AiChatLog> getAiChatLogById(int userId);

    void saveAiResponse(int userId, String text);


}
