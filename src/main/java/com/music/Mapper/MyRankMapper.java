package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyRankMapper {

    List<MyRankWithSong> selectMyRank(@Param("userId")Integer userId);

}
