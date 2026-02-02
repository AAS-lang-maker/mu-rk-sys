package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Song {
    private Integer songId;
    private String songName;
    private String songImg;
    private LocalDateTime songCreateTime;
    private Integer songStatus;
    private Integer singerId;
    private Integer workId;
}
