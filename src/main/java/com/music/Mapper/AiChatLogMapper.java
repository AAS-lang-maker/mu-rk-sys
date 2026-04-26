package com.music.Mapper;

import com.music.pojo.AiChatLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface AiChatLogMapper {
    List<AiChatLog> selectAiChatById(int userId);

    void insertText(int userId, String text);
}
