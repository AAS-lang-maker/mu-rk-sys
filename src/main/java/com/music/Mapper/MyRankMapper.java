package com.music.Mapper;

import com.music.dto.MyRankWithSong;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MyRankMapper {

    List<MyRankWithSong> selectMyRank(@Param("userId")Integer userId,@Param("pageSize")Integer pageSize,
                                      @Param("offset")Integer offset);
    @Select("select count(*) from personal_rank where user_id=#{userId}")
    Integer selectMyRankTotal(@Param("userId")Integer userId);
}
