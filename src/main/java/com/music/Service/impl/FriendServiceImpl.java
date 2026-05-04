package com.music.Service.impl;

import com.music.Mapper.FriendMapper;
import com.music.Service.FriendService;
import com.music.dto.CommonLoveVO;
import com.music.dto.UserSimpleVO;
import com.music.Mapper.UserMapper;
import com.music.pojo.UserFollow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public CommonLoveVO getCommonLoveRank(Integer userId, Integer rankId, Integer sampleLimit) {
        Integer limit = (sampleLimit == null || sampleLimit <= 0) ? 5 : Math.min(sampleLimit, 50);

        Integer count = friendMapper.countMutualFriendLoveRank(userId, rankId);
        if (count == null) {
            count = 0;
        }

        List<String> usernames;
        if (count == 0) {
            log.debug("No mutual friends found for userId {} and rankId {}", userId, rankId);
            usernames = Collections.emptyList();
        } else {
            usernames = friendMapper.listMutualFriendLoveRankUsernames(userId, rankId, limit);
        }

        CommonLoveVO vo = new CommonLoveVO();
        vo.setCommonLoveCount(count);
        vo.setUsernames(usernames);
        return vo;
    }

    @Override
    @Transactional
    public boolean isFollowed(Integer UserId, Integer targetUserId) {
        UserFollow userFollow = friendMapper.isFollowed(UserId, targetUserId);
        if(userFollow == null){
            UserFollow userFollow1 = new UserFollow();
            userFollow1.setUserId(UserId);
            userFollow1.setFollowId(targetUserId);
            userFollow1.setIsMutual(0);
            userFollow1.setCreateTime(LocalDateTime.now());
            friendMapper.add(userFollow1);

            UserFollow userFollow2 = friendMapper.isFollowed(targetUserId, UserId);
            if(userFollow2 != null){
                userFollow2.setIsMutual(1);
                friendMapper.update(userFollow2);
                userFollow1.setIsMutual(1);
                friendMapper.update(userFollow1);
            }
            return true;

        } else {
            friendMapper.delete(UserId, targetUserId);
            return false;
        }
    }

    @Override
    public UserSimpleVO getMymaster(Integer currentUserId) {
        UserSimpleVO userSimpleVO = new UserSimpleVO();
        userSimpleVO.setUserId(currentUserId);
        userSimpleVO.setUsername(userMapper.getUsernameById(currentUserId));
        // 核心改动：直接调用我们刚才写的 SQL 方法
        // 参数1：查谁的列表？查 currentUserId 的
        // 参数2：当前是谁在看？也是 currentUserId（用来计算互关）
        userSimpleVO.setMasterList(friendMapper.selectFollowList(currentUserId, currentUserId)
                .stream().
                map(userFollow -> userMapper.getUsernameById(userFollow.getFollowId()))

                .collect(Collectors.toList()));
        return userSimpleVO;
    }

    @Override
    public UserSimpleVO getMyFans(Integer currentUserId) {
        UserSimpleVO userSimpleVO = new UserSimpleVO();
        userSimpleVO.setUserId(currentUserId);
        userSimpleVO.setUsername(userMapper.getUsernameById(currentUserId));
        userSimpleVO.setFanList(
                friendMapper.selectFanList(currentUserId,currentUserId).
                        stream().
                        map(
                                userFollow -> userMapper.getUsernameById(userFollow.getUserId())
                        ).collect(Collectors.toList()));
        return userSimpleVO;
    }

}

