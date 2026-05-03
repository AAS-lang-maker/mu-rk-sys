package com.music.Service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.music.Mapper.TagsMapper;
import com.music.Mapper.UserTagsMapper;
import com.music.Mapper.rankTagRelMapper;
import com.music.Service.TagsService;
import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.pojo.RankTag;
import com.music.pojo.Tags;
import com.music.pojo.UserTagVO;
import com.music.pojo.UserTags;
import com.music.result.PageResult;
import com.music.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TagsServiceImpl implements TagsService {

    @Autowired
    private TagsMapper tagsMapper; // 操作标签主表
    @Autowired
    private UserTagsMapper userTagsMapper;

    @Autowired
    private rankTagRelMapper rankTagRelMapper;


    @Transactional
    public void cancelFromRanking(Integer rankId,Integer tagId) {
        // 1. 获取当前登录用户
        Integer currentUserId = ThreadLocalUtil.get();

        // 2. 【关键】查询关联关系
        // 假设你有一个方法根据榜单ID和标签ID查询关联实体
        RankTag relation = rankTagRelMapper.selectByRankingIdAndTagId(rankId, tagId,currentUserId);

        // 3. 校验
        if (relation == null) {
            throw new RuntimeException("该榜单下不存在此标签关联");
        }

        // 4. 权限校验：必须是本人添加的关联，才能删除
        if (relation.getUserId() != currentUserId) {
            throw new RuntimeException("无权取消他人添加的标签！");
        }

        // 5. 执行删除（只删关联，不删标签）
        rankTagRelMapper.deleteById(relation.getRTId());
    }
    @Transactional
    public void add(TagsDTO tagsDTO, Integer userId) {
//在库里面添加
        String tagName = tagsDTO.getTagName();
        Tags tags = tagsMapper.selectTagsByName(tagName);
        if (tags == null) {
            tags = new Tags();
            tags.setTagName(tagName);
            tags.setUseCount(1);//新建又添加 加一！
            //"审核成功再添加 或者不审核直接加 现在要跑通就直接加吧 hhh去死"
            tagsMapper.add(tags);
            log.info("添加啦 tag ： {}", tagsDTO);
            if (tags.getTagId() == null) {
                log.error("好小子！！阴我");
                throw new RuntimeException("添加标签失败");
            } else {
                log.info("标签 [{}] 新建成功，ID: {}", tagName, tags.getTagId());
            }

        } else {
            log.info("标签 [{}] 已存在，ID: {}", tagName, tags.getTagId());
            //热度加一
            tagsMapper.incrementUseCount(tags.getTagId());
        }

        UserTags userTag = userTagsMapper.selectByUserIdAndTagId(userId, tags.getTagId());
        if (userTag == null) {
            userTag = new UserTags();
            userTag.setUserId(userId);
            userTag.setTagId(tags.getTagId());
            userTag.setUseCount(1); // 这里的 useCount 是个人次数
            userTag.setCreateTime(LocalDateTime.now());
            userTag.setStatus(1);//可以写18禁了嘻嘻
            userTagsMapper.add(userTag);
        } else {
            userTag.setUseCount(userTag.getUseCount() + 1);
            userTagsMapper.update(userTag);
        }
        log.info("添加成功,关联了捏！！");
    }

//查询 分页展示
    public PageResult pageQueryUserTags( TagsPageQueryDTO tagsPageQueryDTO) {
       // log.info("获取用户自定义云标签，搜索关键词: {}", tagsPageQueryDTO.getKeyword() );

        Integer userId = ThreadLocalUtil.get();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        PageHelper.startPage(tagsPageQueryDTO.getPageNum(), tagsPageQueryDTO.getPageSize());
        String tagName = null;
        if (tagsPageQueryDTO != null) {
            tagName = tagsPageQueryDTO.getTagName();
        }
        List<UserTagVO> userTagVOList = userTagsMapper.selectUserTagsWithDetail(userId, tagName);

        Page<UserTagVO> page =(Page<UserTagVO>) userTagVOList;

        long total = page.getTotal();
        List<UserTagVO> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 删除标签及其相关的歌曲关联
     // ... existing code ...
    // 5. 返回结果
    /**
     * 删除标签及其相关的歌曲关联
     * @param tagId 要删除的标签ID
     */
    @Transactional(rollbackFor = Exception.class) // 【核心】必须加事务，防止删了一半报错
    public void delete(Integer tagId) {

        // 1. 【关键】获取当前登录用户 ID
        Integer currentUserId = ThreadLocalUtil.get(); // 假设这是你获取用户ID的方法


        UserTags tag = userTagsMapper.selectByIdinuser(tagId,currentUserId);
/*
        // 3. 【权限校验】如果不是本人创建的标签，直接抛异常（或返回错误）
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        if (tag.getUserId()!=currentUserId) {
            throw new RuntimeException("无权删除他人的云标签！");
        }*/
        Tags tags= tagsMapper.selectById(tagId);
        if (tags == null) {
            log.warn("尝试删除不存在的标签: {}", tagId);
            return; // 或者抛出自定义异常
        }
        // 防止 useCount 减成负数
        if (tags.getUseCount() > 0) {
            tags.setUseCount(tags.getUseCount() - 1);
        }
        log.info("删除标签：{}", tagId);
        // 第一步：删除歌曲榜单中与该标签相关的所有记录
        // 即：从关联表中删除 tag_id = #{tagId} 的所有行
        rankTagRelMapper.deleteByTagIdinuser(tagId,currentUserId);
        userTagsMapper.deleteByTagIdinuser(tagId,currentUserId);
        //不删除大表 只是一个用户不要了 热度-1
        tagsMapper.updateUseCountfu(tagId);
        log.info("删除成功");
    }
/*
* 修改先从tags_dictionary里面查询 查到就直接换关联 没查到就依据修改新增 然后改关联
 * */
    @Transactional(rollbackFor = Exception.class)
//用户更新自己的云标签库  //榜单自己也有要一起改哦
    public void update(TagsDTO tagsDTO, Integer userId) {

        Integer targetTagId = null;

        Integer oldTagId = tagsDTO.getTagId();
        String newTagName = tagsDTO.getTagName();
        // --- 第一步：查询字典表 ---
        targetTagId = tagsMapper.selectTagIdByName(newTagName);

        Tags tag=new Tags();
        tag.setTagName(newTagName);
        tag.setUseCount(1);
        // --- 第二步：如果没查到，则新增 ---
        if (targetTagId == null) {
            // 这里假设 TagDTO 有一个属性叫 tagName，用于接收插入后的结果
            // 或者直接传入字符串，具体取决于你的 Mapper 写法
            tagsMapper.add(tag);

            // 注意：如果是 MyBatis，insertNewTag 执行后，通常可以直接获取到生成的主键
            // 如果获取不到，可能需要再次查询一次，或者在 insert 语句中配置 useGeneratedKeys
            // 这里为了演示简单，假设 insert 后我们知道了新 ID，或者重新查一次
            // 实际上更严谨的做法是 insert 后重新 select，或者利用 MyBatis 的回显机制

            // 重新查询刚插入的 ID (为了代码逻辑清晰，防止并发问题，实际生产建议用数据库唯一索引或 select for update)
            targetTagId = tagsMapper.selectTagIdByName(newTagName);
            log.info("没查到 新标签已添加，ID: {}", targetTagId);
        }
            // --- 第三步：修改关联 (换) ---
            // 将 user_tags 表中的 oldTagId 替换为 targetTagId
            int rows = tagsMapper.updateUserTagAssociation(userId, oldTagId, targetTagId);
            log.info("用户 [{}] 的标签 [{}] 已更新为 [{}]", userId, oldTagId, targetTagId);
            int rows2 = rankTagRelMapper.updateRankTagAssociation(userId, oldTagId, targetTagId);
            log.info("用户 [{}] 的榜单标签 [{}] 已更新为 [{}]", userId, oldTagId, targetTagId);


        if (rows == 0) {
            // 可选：如果没有更新任何行，说明该用户没有这个旧标签，可以抛出异常或忽略
            throw new RuntimeException("用户未拥有该标签");
        }
        if(rows2==0) {
            log.warn("用户 [{}] 没有该榜单标签 [{}]", userId, oldTagId);
        }
    }


    public PageResult planetary() {
        //PageHelper.startPage(tagsPageQueryDTO.getPageNum(), tagsPageQueryDTO.getPageSize());//只展示前10个
        List<Tags> tags = tagsMapper.planetary();//展示名字和热度
        return new PageResult(10,tags);

    }
}

