package com.music.Mapper;

import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.Comment;
import com.music.pojo.PersonalRank;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MusicHotRankMapper {
    List<MyRankWithSong> listByIds(@Param("rankId") List<Long> rankId);
    Long CountVote(@Param("rankId") Long rankId);
    Long CountLove(@Param("rankId") Long rankId);

    @Delete("delete from vote_record where rank_id=#{rankId} and user_id=#{userId}")
    int deleteVote(Integer userId, Integer rankId);

    @Select("select * from personal_rank where rank_id=#{rankId} ")
    PersonalRank selectRankById(Integer rankId);

    int insertVote(Integer rankId, Integer userId);

    @Delete("delete from love_record where rank_id=#{rankId} and user_id=#{userId};")
    int deleteLove(Integer userId, Integer rankId);

    int insertLove(Integer userId, Integer rankId);

    @Select("select count(*) from vote_record where user_id=#{userId} and rank_id=#{rankId}")
    int countVote(Integer rankId, Integer userId);

    @Select("select count(*) from love_record where user_id=#{userId} and rank_id=#{rankId}")
    int countLove(Integer rankId, Integer userId);

    List<CommentVo> selectComment(Integer rankId, Integer userId);

    Comment selectCommentById(Integer comId);

    @Update("update comment set id_delete=#{idDelete}  where com_id=#{comId}")
    int updateComment(Integer comId, int i);

    int insertLike(Integer userId, Integer comId);

    @Delete("delete from like_comment where com_id=#{comId} and user_id=#{userId}")
    int deleteLike(Integer comId, Integer userId);
}
