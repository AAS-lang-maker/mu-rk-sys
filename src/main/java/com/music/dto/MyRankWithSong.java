package com.music.dto;

import com.music.pojo.rankSong;
import lombok.Data;

import java.util.List;

@Data
public class MyRankWithSong {
   private Integer rankId;
   private String rankName;
   private Integer voteCount;
   private Data updateTime;
   private List<rankSong> rankSongList;
}
