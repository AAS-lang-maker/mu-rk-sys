package com.music.Mapper;

import com.github.pagehelper.Page;
import com.music.dto.TagsPageQueryDTO;
import com.music.pojo.Tags;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagsMapper {


    void add(Tags tags);

    @Delete("DELETE FROM tags_dictionary WHERE tag_id = #{tagId}")
     void deleteById(Integer tagId);

    Page<Tags> pageQuery(@Param("dto")TagsPageQueryDTO tagsPageQueryDTO);

    @Update("UPDATE tags_dictionary SET tag_name = #{tagName},use_count = #{useCount} WHERE tag_id = #{tagId}")
    void update(Tags tags, @Param("tagId") Integer tagId);

    @Select("SELECT * FROM tags_dictionary WHERE tag_name = #{tagName}")
    Tags selectTagsByName(String tagName);

    @Update("UPDATE tags_dictionary SET use_count = use_count + 1 WHERE tag_id = #{tagId}")
    void incrementUseCount(Integer tagId);

    @Select("SELECT * FROM tags_dictionary WHERE tag_id = #{tagId}")
    Tags selectById(int tagId);

    @Select("SELECT tag_id FROM tags_dictionary WHERE tag_name = #{tagName}")
    Integer selectTagIdByName(String tagName);

    @Update("UPDATE user_tags SET tag_id = #{targetTagId} WHERE user_id = #{userId} AND tag_id = #{oldTagId}")
    int updateUserTagAssociation(Integer userId, Integer oldTagId, Integer targetTagId);

    List<Tags> selectList(Integer currentUserId);

    @Select("SELECT * FROM tags_dictionary WHERE tag_id IN (SELECT tag_id FROM rank_tags WHERE rank_id =#{rankId} and user_id = #{userId})")
    List<Tags> selectRankTag(Integer rankId, Integer userId);


    List<Tags> planetary();


    @Select("SELECT rank_id FROM personal_rank WHERE user_id = #{userId}")
    List<Integer> selectRankIdsByUserId(Integer userId);

    @Update("UPDATE tags_dictionary SET use_count = use_count -1 WHERE tag_id = #{tagId}")
    void updateUseCountfu(Integer tagId);

    @Select("SELECT tag_id FROM user_tags WHERE user_id = #{userId}")
    List<Integer> selectTagIdsByUserId(Integer userId);
}
