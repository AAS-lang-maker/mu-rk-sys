package com.music.dto;

import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;
import lombok.Data;

@Data
public class AiCommentVo {
    private PersonalRank personalRank;
    private RankTags rankTags;
    private RankSong rankSong;
}
