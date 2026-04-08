package com.music.Controller;

import com.music.Service.TagsService;
import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.pojo.Tags;
import com.music.result.PageResult;
import com.music.utils.Result;
import groovy.time.BaseDuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/tags")
@RestController
public class TagsController {
    //对于库的 操作
    @Autowired
    private TagsService tagsService;

    //新增标签会保存在词典 然后要对榜单添加时再去添加标签
    @PostMapping("/add")
    public Result add( @RequestBody TagsDTO tagsDTO ){
        log.info("用户自定义云标签");
        log.info("标签 {}",tagsDTO);
        tagsService.add(tagsDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult> page(TagsPageQueryDTO tagsPageQueryDTO) {
        log.info("获取用户自定义云标签");
        PageResult pageResult = tagsService.pageQueryTags(tagsPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        log.info("删除用户自定义云标签");
        tagsService.delete(id);
        return Result.success();
    }

    //
    @PutMapping("/update")
    public Result update(@RequestBody TagsDTO tagsDTO) {
        log.info("更新用户自定义云标签");
        tagsService.update(tagsDTO,tagsDTO.getTagId());
        return Result.success();
    }
}
