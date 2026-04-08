package com.music.Mapper;

import com.music.dto.ApplySongVo;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface AdminSongMapper {
    List<ApplySongVo> selectAllApply();

    @Insert("insert into song(song_name,song_url) values (#{songName},#{songUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")//自增主键
    void insertSong(Song song);

    @Insert("insert into singer(singer_name) values (#{singerName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")//同上
    void insertSinger(Singer singer);
}
