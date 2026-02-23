package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicHotRankMapper {
    List<MyRankWithSong> listByIds(@Param("rankId") List<Long> rankId);
    Long CountVote(@Param("rankId") Long rankId);
    Long CountLove(@Param("rankId") Long rankId);
}
