package com.music.Mapper;

import com.music.pojo.Song;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongMapper {

    Song selectSongById(Integer songId);
}
