package com.music.Mapper;


import com.music.pojo.RankTag;
import com.music.pojo.Tags;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface rankTagRelMapper {
     @Delete("DELETE FROM rank_tags WHERE tag_id = #{tagId}")
     void deleteByTagId( Integer tagId) ;

     @Select("SELECT * FROM rank_tags WHERE rank_id = #{rankId} AND tag_id = #{tagId} and user_id=#{currentUserId}")
     RankTag selectByRankingIdAndTagId(Integer rankId, Integer tagId,Integer currentUserId);

     @Delete("DELETE FROM rank_tags WHERE rt_id = #{rtId}")
     void deleteById(Integer rtId);

     @Delete("delete from rank_tags where tag_id=#{tagId} and user_id=#{currentUserId}")
     void deleteByTagIdinuser(Integer tagId, Integer currentUserId);

     void add(RankTag rankTag);


     List<Tags> selectByRankingIdAndUserId(Integer rankId, Integer userId);

     @Update("UPDATE rank_tags SET tag_id = #{targetTagId} WHERE user_id = #{userId} AND tag_id = #{oldTagId}")
     int updateRankTagAssociation(Integer userId, Integer oldTagId, Integer targetTagId);
}
