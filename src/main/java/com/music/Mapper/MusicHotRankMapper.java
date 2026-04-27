package com.music.Mapper;

import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MusicHotRankMapper {
    List<MyRankWithSong> listByIds(@Param("rankId") List<Long> rankId);
    Integer CountVote(@Param("rankId") Integer rankId);
    Integer CountLove(@Param("rankId") Integer rankId);

    @Delete("delete from vote_record where rank_id=#{rankId} and user_id=#{userId}")
    int deleteVote(@Param("userId") Integer userId, @Param("rankId") Integer rankId);

    @Select("select * from personal_rank where rank_id=#{rankId} ")
    PersonalRank selectRankById(Integer rankId);

    int insertVote(Integer rankId, Integer userId);

    @Delete("delete from love_record where rank_id=#{rankId} and user_id=#{userId};")
    int deleteLove(@Param("userId") Integer userId, @Param("rankId") Integer rankId);

    int insertLove(Integer userId, Integer rankId);

    @Select("select count(*) from vote_record where user_id=#{userId} and rank_id=#{rankId}")
    int countVote(@Param("rankId") Integer rankId, @Param("userId") Integer userId);

    @Select("select count(*) from love_record where user_id=#{userId} and rank_id=#{rankId}")
    int countLove(@Param("rankId") Integer rankId, @Param("userId") Integer userId);

    List<CommentVo> selectComment(Integer rankId, Integer userId);

    Comment selectCommentById(Integer comId);

    @Update("update comment set id_delete=#{idDelete}  where com_id=#{comId}")
    int updateComment(Integer comId, int i);

    int insertLike(Integer userId, Integer comId);

    @Delete("delete from like_comment where com_id=#{comId} and user_id=#{userId}")
    int deleteLike(Integer comId, Integer userId);

    @Select("SELECT d.* \n" +
            "FROM tags_dictionary d\n" +
            "INNER JOIN rank_tags r ON d.tag_id = r.tag_id\n" +
            "WHERE r.rank_id = #{rankId}\n" +
            "ORDER BY d.use_count ASC")
    List<Tags> selectRankTagsxdj(Integer rankId);

    @Select("select * from rank_tags where rank_id=#{rankId}")
    List<RankTags> selectRankTagslistzfm(Integer rankId);
}
