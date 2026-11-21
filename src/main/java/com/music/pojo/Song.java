package com.music.pojo;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Data
public class Song {
    private Integer songId;
    private String songName;
    private String songImg;
    private LocalDateTime songCreateTime;
    private Integer songStatus;
    private Integer singerId;
    private Integer workId;
    private Integer duration;
    private String songurl;
}
