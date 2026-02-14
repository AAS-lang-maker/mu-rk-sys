package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private int comId;
    private int userId;
    private String comContent;
    private Integer parentId;
    private int rankId;
    private int idDelete;
    private LocalDateTime comTime;
}
