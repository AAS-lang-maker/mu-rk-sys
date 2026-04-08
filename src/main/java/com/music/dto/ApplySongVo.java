package com.music.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplySongVo {
    private int demandId;
    private String songName;
    private String singerName;
    private LocalDateTime createTime;
}
