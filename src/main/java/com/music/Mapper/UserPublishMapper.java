package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import com.music.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPublishMapper {
    int insertRank(PersonalRank personalRank);
    //豆包又权威了一回，用返回插入的行数，如果插入的行数大于0，则代表插入成功，Service对应boolean为true
    int sixInsert(@Param("list") List<RankSong> ranksongList);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);

    List<MyRankWithSong> selectPublishRank(@Param("category")Integer category,@Param("pageSize") Integer pageSize,
                                           @Param("offset") Integer offset);

    @Select("select count(*) from personal_rank where category_id=#{categoryId}")
    Integer selectTotal(@Param("category") Integer category);

    Integer insertVote(Integer rankId, String ip);

    int checkip(String ip, Integer rankId);

    int insertLove(Integer userId, String ip, Integer rankId);
    //@Param("list")注解，专门为Maybatis批量插入的需求的List集合起一个别名
}
