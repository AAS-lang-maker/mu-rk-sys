package com.music.Mapper;

import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    List<MyRankWithSong> selectSearch(Integer category, Integer pageNum, Integer offset, Integer pageSize, String keyword);

    Integer selectSearchTotal(Integer category, String keyword);

    void insertComment(Integer rankId, Integer userId, String content, Integer parentId);

    List<CommentVo> selectComment(Integer rankId, Integer userId);

    Comment selectCommentById(Integer comId);

    @Update("update comment set id_delete=#{idDelete}  where com_id=#{comId}")
    int updateComment(Integer comId, int i);

    int insertLike(Integer userId, Integer comId);

    @Delete("delete from like_comment where com_id=#{comId} and user_id=#{userId}")
    int deleteLike(Integer comId, Integer userId);
}
