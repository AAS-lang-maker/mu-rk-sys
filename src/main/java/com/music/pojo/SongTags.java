package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SongTags {
    private int STId;
    private int tagId;
    private int songId;
    private int userId;
    private LocalDateTime createTime;
}
