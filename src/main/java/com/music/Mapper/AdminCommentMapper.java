package com.music.Mapper;

import com.music.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminCommentMapper {
    List<Comment> selectAllComment(Integer pageSize, Integer offset);

    Integer selectAll();

    List<Comment> selectAllComment1(Integer pageSize, Integer offset);

    Integer selectAll1();


    List<Comment> selectAllCom();

    @Update("update comment set risk_score=#{riskScore},comment_content=null,status=#{status},id_delete=#{idDelete} where com_id=#{comId}")
    boolean updateScore1(Comment comment);

    @Update("update comment set status=#{status},risk_score=#{riskScore} where com_id=#{comId}")
    boolean updateScore3(Comment comment);

    @Update("update comment set risk_score=#{riskScore},status=#{status},id_delete=#{idDelete} where com_id=#{comId}")
    boolean updateScore2(Comment comment);

    int deleteComment1(Integer comId);
    int deleteComment2(Integer comId);
}
