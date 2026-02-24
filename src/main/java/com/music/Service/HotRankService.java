package com.music.Service;

import com.music.dto.MyRankWithSong;

import java.util.List;
import java.util.Set;

public interface HotRankService {
    Long caculateHotRank(Long rankId);
    void updateHotRank(Long rankId);
    Set<String> getHotRankId(int start,int end);
    List<MyRankWithSong> listById(List<Long> rankId);
}
