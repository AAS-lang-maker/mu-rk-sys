package com.music.Service;

import com.github.pagehelper.PageInfo;
import com.music.dto.EditRank;
import com.music.dto.MyRankWithSong;
import com.music.pojo.Singer;
import com.music.pojo.Song;

import java.util.List;

public interface MyRankService {

    PageInfo<MyRankWithSong> selectMyrank(Integer pageNum, Integer pageSize,Integer offset, Integer userId);

    MyRankWithSong getRank(Integer rankId);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);

    void insertNewRank(EditRank dto);
}
