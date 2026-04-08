package com.music.Mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface rankTagRelMapper {
     @Delete("DELETE FROM rank_tags WHERE tag_id = #{tagId}")
     void deleteByTagId( Integer tagId) ;
}
