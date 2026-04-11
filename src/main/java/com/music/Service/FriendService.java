package com.music.Service;

import com.music.dto.CommonLoveVO;

public interface FriendService {

    /**
     * 查询：互关好友共同收藏（收藏同一个榜单）
     */
    CommonLoveVO getCommonLoveRank(Integer userId, Integer rankId, Integer sampleLimit);
}

