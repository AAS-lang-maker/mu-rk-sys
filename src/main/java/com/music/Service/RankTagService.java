package com.music.Service;

import com.music.dto.TagsDTO;
import com.music.pojo.Tags;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RankTagService {
    void addTag(Integer rankId, TagsDTO tagDTO);

    List<Tags> getTagsByRank(Integer rankId);
}
