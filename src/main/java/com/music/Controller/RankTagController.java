package com.music.Controller;

import com.music.Mapper.TagsMapper;
import com.music.Service.RankTagService;
import com.music.Service.TagsService;
import com.music.dto.TagOptionVO;
import com.music.dto.TagsDTO;
import com.music.pojo.Tags;
import com.music.utils.Result;
import com.music.utils.ThreadLocalUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Tag;

import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;

@Slf4j
@RestController
@RequestMapping("/api/ranks")
@io.swagger.v3.oas.annotations.tags.Tag(name = "榜单tag管理")
@Transactional
@RequiredArgsConstructor // Lombok 注解，自动生成构造器
public class RankTagController {

    private final RankTagService rankTagService;
    private final TagsMapper tagsMapper;
    private final TagsService tagsService;

    // 2. 路径嵌套：/{rankId}/tags
    @PostMapping("/{rankId}/tags")
    @Operation(summary = "添加标签到榜单")
    public Result addTagToRank(@PathVariable Integer rankId, @RequestBody @Valid TagsDTO tagDTO) {
        // 可以在这里加一层 rankId 的校验，或者直接丢给 Service
        rankTagService.addTag(rankId, tagDTO);
        return Result.success();
    }

    /*
     * 查询回显 - 添加时的下拉标签页
     * 注意：这个接口通常需要登录
     */
    @GetMapping("/tag-options")
    @Operation(summary = "查询标签选项")
    public Result<List<TagOptionVO>> getTagOptions() {
        // 1. 获取当前登录用户的 ID
        Integer currentUserId = ThreadLocalUtil.get();

        // 【优化】增加非空校验，防止未登录访问
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 2. 查询数据库
        List<Tags> myTags = tagsMapper.selectList(currentUserId);

        // 3. Mapping 转换
        List<TagOptionVO> options = myTags.stream()
                .map(tag -> new TagOptionVO(tag.getTagId(), tag.getTagName()))
                .collect(Collectors.toList());

        return Result.success(options);
    }

    // 获取某榜单的已经有的所有云标签
    @GetMapping("/{rankId}/tags")
    @Operation(summary = "获取榜单标签")
    public Result<List<Tags>> getTagsByRank(@PathVariable Integer rankId) {
        // 可以在这里校验 rankId 是否合法，或者直接调用 Service
        return Result.success(rankTagService.getTagsByRank(rankId));
    }

    // 【优化】路径修改，更符合 RESTful 资源层级
    // 语义：删除 /api/ranks/{rankId}/tags/{tagId}
    @DeleteMapping("/{rankId}/tags/{tagId}")
    @Operation(summary = "取消榜单标签")
    public Result cancelFromRanking(@PathVariable Integer rankId, @PathVariable Integer tagId) {
        log.info("取消榜单关联 - 榜单ID: {}, 标签ID: {}", rankId, tagId);

        tagsService.cancelFromRanking(rankId, tagId); // 注意方法名拼写，建议改为 cancelTagFromRanking
        return Result.success();
    }
}

