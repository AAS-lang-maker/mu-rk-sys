package com.music;


import com.music.Service.TagsService;
import com.music.Service.impl.TagsServiceImpl;
import com.music.dto.TagsPageQueryDTO;
import com.music.Mapper.UserTagsMapper;
import com.music.utils.ThreadLocalUtil;
import com.music.pojo.UserTagVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.music.result.PageResult; // 注意：这里指 PageHelper 自带的 PageResult，或者是你自己定义的
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class miao {

    @Mock
    private UserTagsMapper userTagsMapper;

    // 如果被测试类中有 tagsMapper，也需要在这里 Mock
    // @Mock
    // private TagsMapper tagsMapper;

    @InjectMocks
    private TagsServiceImpl userTagsService;

    private Integer mockUserId = 1001;

    @BeforeEach
    void setUp() {
        // 模拟 ThreadLocalUtil.get() 返回固定的用户ID
        // 注意：ThreadLocal 的 Mock 比较特殊，这里假设它是静态方法
        //try (MockedStatic<ThreadLocalUtil> mockedStatic = mockStatic(ThreadLocalUtil.class)) {
          //  ThreadLocalUtil.mockStaticGet(mockUserId);
            // 如果 ThreadLocalUtil.get() 是普通静态方法，直接 when(ThreadLocalUtil.get()).thenReturn(mockUserId);

    }

    @Test
    void testPageQueryUserTags_Success() {
        // --- 1. 准备数据 (Arrange) ---

        // 模拟分页参数
        TagsPageQueryDTO queryDTO = new TagsPageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setTagName("Java");

        // 模拟数据库返回的结果集
        List<UserTagVO> mockList = new ArrayList<>();
        UserTagVO vo = new UserTagVO();
        vo.setUserId(1);
        vo.setTagName("Java开发");
        vo.setUseCount(999);
        mockList.add(vo);

        // 模拟 PageHelper 的行为：
        // 当调用 selectUserTagsWithDetail 时，返回一个被包装成 Page 对象的 List
        // 在实际测试中，我们通常直接模拟 Mapper 返回一个 Page 对象
        Page<UserTagVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page.addAll(mockList);
        page.setTotal(1); // 设置总记录数

        // --- 2. 定义 Mock 行为 (Act) ---

        // 模拟 ThreadLocal 获取用户ID
        try (MockedStatic<ThreadLocalUtil> mockedStatic = mockStatic(ThreadLocalUtil.class)) {
            mockedStatic.when(ThreadLocalUtil::get).thenReturn(mockUserId);

            // 模拟 Mapper 查询返回分页数据
            // 注意：这里假设你的 Service 调用的是 selectUserTagsWithDetail
            when(userTagsMapper.selectUserTagsWithDetail(eq(mockUserId), eq("Java")))
                    .thenReturn(page);

            // --- 3. 执行测试 (Invoke) ---
            com.music.result.PageResult result = userTagsService.pageQueryUserTags(queryDTO);

            // --- 4. 断言验证 (Assert) ---

            // 验证总条数是否正确
            assertEquals(1, result.getTotal());

            // 验证返回的列表是否为空
            assertNotNull(result.getRecords());
            assertFalse(result.getRecords().isEmpty());

            UserTagVO resultVO = (UserTagVO) result.getRecords().get(0);
            // 验证列表中的具体数据
            assertEquals("Java开发",resultVO.getTagName());
            assertEquals(999,resultVO.getUseCount());

            // 验证 Mapper 方法是否被调用了一次
            verify(userTagsMapper, times(1)).selectUserTagsWithDetail(mockUserId, "Java");
        }
    }

    @Test
    void testPageQueryUserTags_NoLogin() {
        // --- 1. 准备数据 ---
        TagsPageQueryDTO queryDTO = new TagsPageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        // --- 2. 模拟未登录情况 ---
        try (MockedStatic<ThreadLocalUtil> mockedStatic = mockStatic(ThreadLocalUtil.class)) {
            // 模拟返回 null
            mockedStatic.when(ThreadLocalUtil::get).thenReturn(null);

            // --- 3. 执行并期望抛出异常 ---
            Exception exception = assertThrows(RuntimeException.class, () -> {
                userTagsService.pageQueryUserTags(queryDTO);
            });

            // --- 4. 验证异常信息 ---
            assertEquals("用户未登录", exception.getMessage());

            // 验证 Mapper 是否未被调用（因为提前抛异常了）
            verify(userTagsMapper, never()).selectUserTagsWithDetail(anyInt(), anyString());
        }
    }
}