package com.music.Mapper;

import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPublishMapper {
    int insertRank(PersonalRank personalRank);
    //豆包又权威了一回，用返回插入的行数，如果插入的行数大于0，则代表插入成功，Service对应boolean为true
    int sixInsert(@Param("list") List<RankSong> ranksongList);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);
    //@Param("list")注解，专门为Maybatis批量插入的需求的List集合起一个别名
}
