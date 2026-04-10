package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SongDemand {
    private int demandId;
    private int isDelete;
    private String demandSongName;
    private String demandSingerName;
    private LocalDateTime createTime;
}
