package com.music;

import com.music.Service.HotRankService;
import com.music.Service.TagsService;
import com.music.dto.TagsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HotRankInitTest {
    @Autowired
    private HotRankService hotRankService;

    @Autowired
    private TagsService tagsService;
  @Test
    public void hotRankInitTest(){
        hotRankService.updateHotRank(12);
        hotRankService.updateHotRank(13);
        hotRankService.updateHotRank(14);
    }

    @Test
    void testAddNewTag() {
        // 模拟用户 ID (假设你 ThreadLocal 里能手动塞或者数据库里有 ID 为 1 的用户)
        Integer mockUserId = 1;

        // 准备数据
        TagsDTO dto = new TagsDTO();
        dto.setTagName("测试新标签" + System.currentTimeMillis()); // 加时间戳防止重名

        System.out.println(">>> 开始测试添加新标签...");
        tagsService.add(dto, mockUserId);
        System.out.println(">>> 测试完成，请检查数据库！");
    }

    /**
     * 测试场景 2：重复添加同一个标签（模拟热度增加）
     * 预期：
     * 1. Tag 表 use_count 再 + 1 (全站热度涨)
     * 2. UserTag 表 use_count 再 + 1 (个人次数涨)
     */
    @Test
    void testAddExistingTag() {
        Integer mockUserId = 1;
        TagsDTO dto = new TagsDTO();
        dto.setTagName("周杰伦"); // 用一个固定的名字

        System.out.println(">>> 第一次添加...");
        tagsService.add(dto, mockUserId);

        System.out.println(">>> 第二次添加（模拟热度增加）...");
        tagsService.add(dto, mockUserId);

        System.out.println(">>> 测试完成，请检查数据库 use_count 是否变为 2！");
    }
}
