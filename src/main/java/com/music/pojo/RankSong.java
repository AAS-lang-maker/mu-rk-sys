package com.music.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class RankSong {
    private Integer rsId;
    private Integer rankId;
    private Integer ranking;
    private Integer songId;
    private String songName;
    private String singerName;
    private Integer voteCount;
    private Date rankCreateTime;
    private Date rankUpdateTime;

}
