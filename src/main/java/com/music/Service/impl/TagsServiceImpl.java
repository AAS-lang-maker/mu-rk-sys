package com.music.Service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.music.Mapper.TagsMapper;
import com.music.Mapper.rankTagRelMapper;
import com.music.Service.TagsService;
import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.pojo.Tags;
import com.music.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TagsServiceImpl implements TagsService {

    @Autowired
    private TagsMapper tagsMapper; // 操作标签主表
    @Autowired
    private rankTagRelMapper rankTagRelMapper;         // 操作歌曲与标签的关联表
    public void add(TagsDTO tagsDTO) {
//在库里面添加
        log.info("添加啦 tag ： {}",tagsDTO);
        Tags tags = new Tags();
        BeanUtils.copyProperties(tagsDTO,tags);
        tagsMapper.add(tags);
        log.info("添加成功");
    }


    public PageResult pageQueryTags(TagsPageQueryDTO tagsPageQueryDTO) {
        log.info("获取用户自定义云标签");
        //开始分页查询
            PageHelper.startPage(tagsPageQueryDTO.getPage(), tagsPageQueryDTO.getPageSize());
            Page<Tags> page = tagsMapper.pageQuery(tagsPageQueryDTO);
            long total = page.getTotal();
            List<Tags> records = page.getResult();

        return new PageResult(total,records);
    }
    /**
     * 删除标签及其相关的歌曲关联
     * @param tagId 要删除的标签ID
     */
    @Transactional(rollbackFor = Exception.class) // 【核心】必须加事务，防止删了一半报错
    public void delete(Integer tagId) {
        log.info("删除标签：{}", tagId);
        // 第一步：删除歌曲榜单中与该标签相关的所有记录
        // 即：从关联表中删除 tag_id = #{tagId} 的所有行
        rankTagRelMapper.deleteByTagId(tagId);

        // 第二步：删除标签字典表中的这个标签本身
        tagsMapper.deleteById(tagId);

        log.info("删除成功");
    }


    public void update(TagsDTO tagsDTO, Integer tagId) {

        log.info("更新标签：{}", tagsDTO);
        Tags tags = new Tags();
        BeanUtils.copyProperties(tagsDTO,tags);
        tagsMapper.update(tags,tagId);

    }
    // 注意：如果还有其他的关联表（比如歌单标签关联），也需要在这里继续写删除逻辑




}
