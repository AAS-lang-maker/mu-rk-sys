package com.music.Service;

import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.result.PageResult;
import org.springframework.stereotype.Service;


@Service
public interface TagsService {


    void cancelFromRanking(Integer rankId,Integer tagId);

    void add(TagsDTO tagsDTO, Integer userId);



    PageResult pageQueryUserTags(TagsPageQueryDTO tagsPageQueryDTO);

    void delete(Integer id);

    void update(TagsDTO tagsDTO, Integer tagId);


    PageResult planetary();
}
