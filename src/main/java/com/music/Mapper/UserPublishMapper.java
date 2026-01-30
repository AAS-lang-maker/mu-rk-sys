package com.music.Mapper;

import com.music.pojo.personalRank;
import com.music.pojo.rankSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPublishMapper {
    personalRank insertRank(personalRank personalRank);
    //返回插入的行数，如果插入的行数大于0，则代表插入成功，Service对应boolean为true
    int sixInsert(@Param("list") List<rankSong> ranksongList);//豆包又权威了一回，用
    //@Param("list")注解，专门为Maybatis批量插入的需求的List集合起一个别名
}
