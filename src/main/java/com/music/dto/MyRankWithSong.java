package com.music.dto;

import com.music.pojo.RankSong;
import com.music.pojo.RankTagVO;
import com.music.pojo.RankTags;
import lombok.AccessLevel;
import com.music.pojo.UserTags;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
public class MyRankWithSong {
   private Integer rankId;
   private String rankName;
   private Integer voteCount;
   //private String songName;
   private String username;
   private Integer loveCount;
   private LocalDateTime publishTime;//数据库中的datetime对应java类中的localdateTime
    //date对应LocalDate
   private List<RankSong> rankSongList;

   private boolean Followed;
   private RankTagVO rankTagVOList;
   private List<RankTags> rankTagsList;

   private List<UserTags> userTagList;
}
