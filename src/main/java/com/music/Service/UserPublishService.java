package com.music.Service;

import com.github.pagehelper.PageInfo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.utils.Result;

import java.util.List;


public interface UserPublishService {
    public boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);

    PageInfo<MyRankWithSong> selectPublishRank(Integer category, Integer pageNum, Integer pageSize, Integer offset);

    boolean insertVote(Integer userId, Integer rankId, String ip);

    boolean insertLove(Integer userId, String ip, Integer rankId);
}
