package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatLog {
  private int AIId;
    private int userId;
    private String role;
    private LocalDateTime createTime;
}
