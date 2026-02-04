package com.music.Service;

import com.github.pagehelper.PageInfo;
import com.music.dto.MyRankWithSong;

import java.util.List;

public interface MyRankService {

    PageInfo<MyRankWithSong> selectMyrank(Integer pageNum, Integer pageSize,Integer offset, Integer userId);
}
