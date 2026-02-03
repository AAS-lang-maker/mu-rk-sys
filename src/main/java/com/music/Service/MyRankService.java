package com.music.Service;

import com.music.dto.MyRankWithSong;

import java.util.List;

public interface MyRankService {

    List<MyRankWithSong> selectMyrank(Integer urluserId);
}
