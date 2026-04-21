package com.music.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT username FROM user WHERE user_id = #{currentUserId}")
    String getUsernameById(Integer currentUserId);
}
