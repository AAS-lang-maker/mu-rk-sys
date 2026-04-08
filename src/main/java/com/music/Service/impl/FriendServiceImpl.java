package com.music.Service.impl;

import com.music.Mapper.FriendMapper;
import com.music.Service.FriendService;
import com.music.dto.CommonLoveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;

    @Override
    public CommonLoveVO getCommonLoveRank(Integer userId, Integer rankId, Integer sampleLimit) {
        Integer limit = (sampleLimit == null || sampleLimit <= 0) ? 5 : Math.min(sampleLimit, 50);

        Integer count = friendMapper.countMutualFriendLoveRank(userId, rankId);
        if (count == null) {
            count = 0;
        }

        List<String> usernames;
        if (count == 0) {
            usernames = Collections.emptyList();
        } else {
            usernames = friendMapper.listMutualFriendLoveRankUsernames(userId, rankId, limit);
        }

        CommonLoveVO vo = new CommonLoveVO();
        vo.setCommonLoveCount(count);
        vo.setUsernames(usernames);
        return vo;
    }
}

