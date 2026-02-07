package com.music.Service;

import com.github.pagehelper.PageInfo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.pojo.Work;

import java.util.List;

public interface WorkPunlishService {
    PageInfo<MyRankWithSong> selectAllRank(Integer offset, Integer category, Integer pageNum, Integer pageSize);

    boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto);

    List<Work> selectWork(Integer categoryId);

    List<Song> selectSong(Integer workId);

    boolean insertVote(Integer userId, Integer rankId, String ip);

    boolean insertLove(Integer userId, String ip, Integer rankId);
}
