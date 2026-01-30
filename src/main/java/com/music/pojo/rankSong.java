package com.music.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class rankSong {
    private Integer rsId;
    private Integer rankId;
    private Integer ranking;
    private Integer songId;
    private Date rankCreateTime;
    private Date rankUpdateTime;
}
