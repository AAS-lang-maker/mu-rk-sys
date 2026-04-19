package com.music.Service;

import com.music.dto.MyRankWithSong;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;

public interface AiCommentService {
    String getAiComment(MyRankWithSong myRankWithSong);
}
