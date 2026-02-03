package com.music.dto;

import com.music.pojo.RankSong;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyRankWithSong {
   private Integer rankId;
   private String rankName;
   private Integer voteCount;
   private LocalDateTime updateTime;//数据库中的datetime对应java类中的localdateTime
    //date对应LocalDate
   private List<RankSong> rankSongList;
}
