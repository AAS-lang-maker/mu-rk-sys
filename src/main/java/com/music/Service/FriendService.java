package com.music.Service;

import com.music.dto.CommonLoveVO;
import com.music.dto.UserSimpleVO;
import org.apache.ibatis.annotations.Select;

public interface FriendService {

    /**
     * 查询：互关好友共同收藏（收藏同一个榜单）
     */
    CommonLoveVO getCommonLoveRank(Integer userId, Integer rankId, Integer sampleLimit);

    /**
     * 操作：关注/取消关注
     */
    boolean isFollowed(Integer currentUserId, Integer targetUserId);

    UserSimpleVO getMyFans(Integer currentUserId);

    UserSimpleVO getMymaster(Integer currentUserId);
}

