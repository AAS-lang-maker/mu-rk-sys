package com.music.Service;

import com.music.dto.RankAddRequest;
import com.music.utils.Result;


public interface UserPublishService {
    boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) ;
}
