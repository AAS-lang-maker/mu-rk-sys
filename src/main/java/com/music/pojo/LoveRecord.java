package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoveRecord {
    private Integer loveId;
    private Integer userId;
    private Integer love;
    private String ip;
    private Integer loveStatus;
    private Integer rankId;
    private LocalDateTime loveTime;
}
