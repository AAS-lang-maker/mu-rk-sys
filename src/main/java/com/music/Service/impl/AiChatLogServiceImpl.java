package com.music.Service.impl;

import com.music.Mapper.AiChatLogMapper;
import com.music.Service.AiChatLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




@Service
public class AiChatLogServiceImpl implements AiChatLogService {

    @Autowired
    private AiChatLogMapper aiChatLogMapper;

    @Override
    public void saveAiResponse(int userId, String text) {
        aiChatLogMapper.insertText(userId,text);
    }

}
