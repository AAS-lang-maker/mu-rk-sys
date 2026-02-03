package com.music.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class RankSong {
    private Integer rsId;
    private Integer rankId;
    private Integer ranking;
    private Integer songId;
    private Date rankCreateTime;
    private Date rankUpdateTime;
}
