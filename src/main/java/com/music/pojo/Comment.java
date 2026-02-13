package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private int commentId;
    private int userId;
    private String commentContent;
    private int parentId;
    private int rankId;
    private int idDelete;
    private LocalDateTime commentTime;
}
