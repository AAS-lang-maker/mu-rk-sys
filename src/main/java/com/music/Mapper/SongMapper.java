package com.music.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.pojo.Song;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongMapper extends BaseMapper<Song> {

    Song selectSongById(Integer songId);
}
