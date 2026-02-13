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

    Integer insertVote(Integer rankId);

   // int checkip(String ip, Integer rankId);

    int insertLove(Integer userId,String ip, Integer rankId);

    int checkLoveip(String ip, Integer rankId);

    List<MyRankWithSong> selectSearch(Integer category, Integer pageNum, Integer offset, Integer pageSize, String keyword);


    Integer selectSearchTotal(Integer category, String keyword);

    @Update("update personal_rank set vote_count=#{rows} where rank_id=#{rankId}")
    int updateVoteCount(@Param("rankId") Integer rankId,@Param("rows") int rows);

    @Update("update personal_rank set love_count=#{row1} where rank_id=#{rankId} and user_id=#{userId}")
    int updateLoveCount(Integer userId, int row1, Integer rankId);

    void insertComment(Integer rankId, Integer userId, String content, Integer parentId);

    List<CommentVo> selectComment(Integer rankId, Integer comId);

    Integer insertLike(Integer userId,Integer comId);

    Comment selectCommentById(Integer comId);

    @Update("update comment set id_delete=#{idDelete}  where com_id=#{comId}")
    int updateLike(Integer comId, int idDelete);

    @Delete("delete from like_comment where com_id=#{comId} and user_id=#{userId}")
    int deleteLike(Integer comId, Integer userId);
    //?应该可以不用@Param，对于update操作
    //@Param("list")注解，专门为Maybatis批量插入的需求的List集合起一个别名
}
