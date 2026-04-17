package com.music.Service;

import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;

public interface AiCommentService {
    String getAiComment(RankSong rankSong, PersonalRank personalRank, RankTags rankTags);
}
