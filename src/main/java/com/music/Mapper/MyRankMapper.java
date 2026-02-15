package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import com.music.pojo.RankSong;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.pojo.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MyRankMapper {

    List<MyRankWithSong> selectMyRank(@Param("userId")Integer userId,@Param("pageSize")Integer pageSize,
                                      @Param("offset")Integer offset);
    @Select("select count(*) from personal_rank where user_id=#{userId}")
    Integer selectMyRankTotal(@Param("userId")Integer userId);

    MyRankWithSong getRank(Integer rankId);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);

    @Update("update personal_rank set rank_name=#{rankName},publish_time=now() where rank_id=#{rankId}")
    void insertNewRankname(@Param("rankId") Integer rankId,@Param("rankName") String rankName);
    @Delete("delete from rank_song where rank_id=#{rankId}")
    void deleteOldRankname(Integer rankId);

    void insertNewRank(List<RankSong> newSongs);

    List<MyRankWithSong> selectLoveSong(Integer userId, Integer pageSize, Integer offset, Integer pageNum);

    @Select("select count(*) from love_record where user_id=#{userId}")
    Integer selectloveTotal(Integer userId);

    @Delete("delete from personal_rank where rank_id=#{rankId}")
    boolean deleteRank(Integer rankId);

    UserInfo selectByUserId(Integer userId);
}
