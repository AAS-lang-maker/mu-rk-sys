package com.music.Service;

import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.result.PageResult;
import org.springframework.stereotype.Service;


@Service
public interface TagsService {


    void add(TagsDTO tagsDTO);



    PageResult pageQueryTags(TagsPageQueryDTO tagsPageQueryDTO);

    void delete(Integer id);

    void update(TagsDTO tagsDTO, Integer tagId);
}
