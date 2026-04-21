package com.music.Controller;

import com.music.Service.TagsService;
import com.music.dto.TagsDTO;
import com.music.dto.TagsPageQueryDTO;
import com.music.result.PageResult;
import com.music.utils.Result;
import com.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/tagsplanet")
@RestController
@Tag(name = "标签管理", description = "标签管理相关接口")
public class TagsController {
    //对于库的 操作
    @Autowired
    private TagsService tagsService;


    // 如果你用了本地词库方案，可以在这里注入
    // @Autowired
    // private SensitiveFilter sensitiveFilter;


    //新增标签会保存在词典 然后要对榜单添加时再去添加标签 先建后添加 ！！！
    //敏感词嘻嘻
    @PostMapping("/add")
    @Operation(summary = "添加标签", description = "添加标签")
    public Result add( @RequestBody TagsDTO tagsDTO ){
        log.info("用户自定义云标签");
        Integer userId = ThreadLocalUtil.get();
        if(userId == null){
            return Result.error("请先登录");
        }
        if(tagsDTO.getTagName()==null||tagsDTO.getTagName().trim().isEmpty()){
            return Result.error("标签不能为空哦 喵 小猫喜欢你");
        }

        log.info(  "添加人：{}  标签： {}",userId,tagsDTO);
     //防止失败
     try {
         tagsService.add(tagsDTO, userId);
         return Result.success("喵，你已经成功添加");
     }catch (Exception e){
         return Result.error("添加失败");
     }
     }

    //展示自己的page emmm 要不要分页呢？分页 吧 写都写了打死我 喵

    //系统标签库 热度 展示
    @GetMapping("/planetall")
    @Operation(summary = "获取系统标签云", description = "获取系统标签云")
    public Result<PageResult> planetall() {
        log.info("获取系统标签云");
        PageResult pageResult = tagsService.planetary();
        return Result.success(pageResult);
    }



    @GetMapping("/selectpage")
    @Operation(summary = "获取用户自定义云标签", description = "获取用户自定义云标签")
    //个人标签库查询展示
    public Result<PageResult> selectpage(TagsPageQueryDTO tagsPageQueryDTO) {
        log.info("获取用户自定义云标签");
        PageResult pageResult = tagsService.pageQueryUserTags(tagsPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/planet/{id}")
    @Operation(summary = "删除用户自定义云标签", description = "删除用户自定义云标签")
    public Result delete(@PathVariable Integer id) {
        log.info("删除用户自定义云标签");
        //在星球删了 不是在榜单删除 ！！！所以所有关联关系全删
        tagsService.delete(id);
        log.info("删除成功");
        return Result.success();
    }
   /* //2. 榜单页面取消（仅解除关联）
    //2. 榜单页面取消（仅解除关联）
    @DeleteMapping("/ranking/{id}")
    public Result cancelFromRanking(@PathVariable Integer id, @RequestParam Integer rankId) {
        log.info("取消用户自定义云标签在榜单，标签ID: {}, 榜单ID: {}", id, rankId);

        tagsService.cancelFromRanking(rankId, id);
        return Result.success();
    }*/


    //
    @PutMapping("/update")
    @Operation(summary = "更新用户自定义云标签", description = "更新用户自定义云标签")
    public Result update(@RequestBody TagsDTO tagsDTO) {
        log.info("更新用户自定义云标签");

        Integer userId = ThreadLocalUtil.get();
        tagsService.update(tagsDTO,userId);
        return Result.success();
    }
}
