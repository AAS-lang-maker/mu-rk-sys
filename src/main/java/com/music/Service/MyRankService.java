package com.music.Service;

import com.music.dto.MyRankWithSong;
import com.music.pojo.personalRank;
import com.music.utils.Result;

import java.util.List;

public interface MyRankService {

    List<MyRankWithSong> selectMyrank(Integer urluserId);
}
