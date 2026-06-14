package com.music.Service.impl;

import com.music.Mapper.MyRankMapper;
import com.music.Mapper.TagsMapper;
import com.music.Mapper.UserTagsMapper;
import com.music.Mapper.rankTagRelMapper;
import com.music.Service.RankTagService;
import com.music.dto.TagsDTO;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankTag;
import com.music.pojo.Tags;
import com.music.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class RankTagServiceImpl implements RankTagService {


    @Autowired
    private TagsMapper tagsMapper;
    @Autowired
    private rankTagRelMapper rankTagRelMapper;

    @Autowired
    private UserTagsMapper userTagsMapper;
    @Autowired
    private MyRankMapper myRankMapper;


    /*
* 添加 标签
* */
    @Override
    public void addTag(Integer rankId, TagsDTO tagDTO) {
        Integer userId= ThreadLocalUtil.get();
      /*  Tags tags = new Tags();
        tags.setTagId(tagDTO.getTagId());
        tags.setTagName(tagDTO.getTagName());
        tags.setUseCount(tagDTO.getUseCount()+1);*/

        userTagsMapper.update(userTagsMapper.selectByIdinuser(tagDTO.getTagId(),userId));

        RankTag rankTag = new RankTag();
        rankTag.setTagId(tagDTO.getTagId());
        rankTag.setRankId(rankId);
        rankTag.setUserId(userId);
        rankTag.setCreateTime(LocalDateTime.now());
        rankTagRelMapper.add(rankTag);
    }
/*
* 展示！
* */

    public List<Tags> getTagsByRank(Integer rankId) {
        PersonalRank personalRank = myRankMapper.selectRankById(rankId);
        if (personalRank == null) {
            log.warn("【获取标签】榜单不存在！rankId={}", rankId);
            return null;
        }
        Integer creatorId= myRankMapper.selectCreatorIdByRankId(rankId);
        List<Tags> tagsList =  rankTagRelMapper.selectByRankingIdAndUserId(rankId,creatorId);
        return tagsList;
    }
}
