package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.Song;
import com.music.pojo.Work;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkPublishMapper {
    List<MyRankWithSong> selectAllRank(Integer category, Integer pageSize, Integer offset);

    @Select("select count(*) from personal_rank where category_id=#{category}")
    Integer selectTotal(Integer category);

    void insertRank(PersonalRank personalRank);

    int sixInsert(List<RankSong> ranksongList);

    List<Work> selectWork(Integer categoryId);

    List<Song> selectSong(Integer workId);

    @Select("select count(*) from vote_record where ip=#{ip} and rank_id=#{rankId}")
    int checkip(String ip, Integer rankId);

    int insertVote(Integer rankId, String ip);

    int insertLove(Integer userId, String ip, Integer rankId);
}
