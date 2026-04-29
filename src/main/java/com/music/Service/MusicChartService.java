package com.music.Service;

import com.music.dto.MyRankWithSong;

import java.util.List;

public interface MusicChartService {
    List<MyRankWithSong> getMusicChart(String keyword,int userId);
    String getHotChart();
}
