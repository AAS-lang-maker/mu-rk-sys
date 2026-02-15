package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoveRecord {
    private Integer loveId;
    private Integer userId;
    private String ip;
    private Integer rankId;
    private LocalDateTime loveTime;
}
