package com.music.Mapper;

import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.*;
import org.apache.ibatis.annotations.*;

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

    @Select("select count(*) from personal_rank where category_id=#{category}")
    Integer selectTotal(@Param("category") Integer category);

   // int checkip(String ip, Integer rankId);
    //int checkLoveip(String ip, Integer rankId);

    List<MyRankWithSong> selectSearch(Integer category, Integer pageNum, Integer offset, Integer pageSize, String keyword);


    Integer selectSearchTotal(Integer category, String keyword);

    @Update("update personal_rank set vote_count=#{rows} where rank_id=#{rankId}")
    int updateVoteCount(@Param("rankId") Integer rankId,@Param("rows") int rows);

    @Update("update personal_rank set love_count=#{row1} where rank_id=#{rankId} and user_id=#{userId}")
    int updateLoveCount(Integer userId, int row1, Integer rankId);

    void insertComment(Integer rankId, Integer userId, String content, Integer parentId);

    List<CommentVo> selectComment(@Param("rankId") Integer rankId,@Param("userId") Integer userId);

    Integer insertLike(Integer userId,Integer comId);

    Comment selectCommentById(Integer comId);

    @Update("update comment set id_delete=#{idDelete}  where com_id=#{comId}")
    int updateComment(Integer comId, int idDelete);

    @Delete("delete from like_comment where com_id=#{comId} and user_id=#{userId}")
    int deleteLike(Integer comId, Integer userId);

    @Select("select * from personal_rank where rank_id=#{rankId} ")
    PersonalRank selectRankById(Integer rankId);

    int insertVote(Integer rankId, Integer userId);

    int insertLove(Integer userId, Integer rankId);

    @Delete("delete from vote_record where rank_id=#{rankId} and user_id=#{userId}")
    int deleteVote(Integer rankId, Integer userId);

    @Delete("delete from love_record where rank_id=#{rankId} and user_id=#{userId};")
    int deleteLove(Integer rankId, Integer userId);

    @Select("select count(*) from vote_record where user_id=#{userId} and rank_id=#{rankId}")
    int countVote(@Param("rankId") Integer rankId,@Param("userId") Integer userId);

    @Select("select count(*) from love_record where user_id=#{userId} and rank_id=#{rankId}")
    int countLove(Integer rankId, Integer userId);
    //@Param("list")注解，专门为Maybatis批量插入的需求的List集合起一个别名
}
