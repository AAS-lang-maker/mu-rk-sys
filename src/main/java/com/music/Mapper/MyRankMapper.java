package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyRankMapper {

    List<MyRankWithSong> selectMyRank();

}
