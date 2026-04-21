package com.music.Mapper;

import com.github.pagehelper.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.music.pojo.UserTagVO;
import com.music.pojo.UserTags;
import lombok.Data;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserTagsMapper {
    @Select("select * from user_tags where tag_id= #{tagId}")
     UserTags selectById(Integer tagId);

    @Insert("insert into user_tags(tag_id,user_id,create_time,status) values(#{tagId},#{userId},#{createTime},#{status})")
    void add(UserTags userTags);

    @Select("select * from user_tags where user_id=#{userId} and tag_id=#{tagId}")
    UserTags selectByUserIdAndTagId(Integer userId, Integer tagId);

@Update("update user_tags set use_count=use_count+1 where tag_id=#{tagId} and user_id=#{userId}")
    void update(UserTags userTag);


    @Select("select * from user_tags where user_id=#{userId}")
    List<UserTags> selectByUserId(Integer userId);

    List<UserTagVO> selectUserTagsWithDetail(@Param("userId")Integer userId,
                                             @Param("tagName") String tagName);

    @Delete("delete from user_tags where tag_id=#{tagId} and user_id=#{currentUserId}")
    void deleteByTagIdinuser(Integer tagId, Integer currentUserId);

@Select("select * from user_tags where user_id=#{currentUserId} and tag_id=#{tagId}")
    UserTags selectByIdinuser(Integer tagId, Integer currentUserId);
}
