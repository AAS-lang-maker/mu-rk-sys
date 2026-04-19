package com.music.dto;

import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 必须：Jackson 需要无参构造
@AllArgsConstructor
public class AiCommentVo {
    private PersonalRank personalRank;
    private RankTags rankTags;
    private RankSong rankSong;
}
