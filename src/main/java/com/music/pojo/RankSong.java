package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor  // 必须：Jackson 需要无参构造
@AllArgsConstructor
public class RankSong {
    private Integer rsId;
    private Integer rankId;
    private Integer ranking;
    private Integer songId;
    private String songName;
    private String singerName;
    private Date rankCreateTime;
    private Date rankUpdateTime;

}
