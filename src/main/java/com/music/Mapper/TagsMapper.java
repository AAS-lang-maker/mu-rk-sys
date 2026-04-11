package com.music.Mapper;

import com.github.pagehelper.Page;
import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.pojo.Tags;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface TagsMapper {


    void add(Tags tags);

    @Delete("DELETE FROM tags_dictionary WHERE tag_id = #{tagId}")
     void deleteById(Integer tagId);

    Page<Tags> pageQuery(TagsPageQueryDTO tagsPageQueryDTO);

    @Update("UPDATE tags_dictionary SET tag_name = #{tagName} WHERE tag_id = #{tagId}")
    void update(Tags tags, Integer tagId);
}
