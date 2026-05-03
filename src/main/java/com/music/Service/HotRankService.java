package com.music.Service;

import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;

import java.util.List;
import java.util.Set;

public interface HotRankService {
    Integer caculateHotRank(Integer rankId);
    void updateHotRank(Integer rankId);
    Set<String> getHotRankId(int start,int end);
    List<MyRankWithSong> listById(List<Long> rankId);

    boolean insertVote(Integer userId, Integer rankId);

    boolean insertLove(Integer userId, Integer rankId);

    boolean deleteVote(Integer rankId, Integer userId);

    boolean deleteLove(Integer rankId, Integer userId);

    List<CommentVo> selectComment(Integer rankId, Integer userId);

    boolean deleteComment(Integer comId, Integer userId);

    boolean insertLike(Integer userId, Integer comId);

    boolean deleteLike(Integer comId, Integer userId);
}
