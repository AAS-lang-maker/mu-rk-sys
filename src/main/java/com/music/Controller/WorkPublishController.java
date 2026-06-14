package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.impl.WorkPublishServiceImpl;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Comment;
import com.music.pojo.Song;
import com.music.pojo.Work;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/workrank")
@Tag(name = "WorkPublishController", description = "作品发布控制器")
public class WorkPublishController {
    @Resource


    private WorkPublishServiceImpl workPublishServiceImpl;
    @GetMapping("/publish")
    @Operation(summary = "发布作品", description = "发布作品")
    public String publish(@RequestParam("token")String token,
                           RedirectAttributes redirectAttributes,@RequestParam("category")Integer category,
                         @RequestParam("pageNum")Integer pageNum, @RequestParam("pageSize")Integer pageSize
                        , Model model) throws Exception {
     if(token==null||token.isEmpty()){
         redirectAttributes.addFlashAttribute("errormessage","token不能为空");
         return "redirect:/login.html";
     }

       Integer offset=(pageNum-1)*pageSize;
       PageInfo<MyRankWithSong> workRank=workPublishServiceImpl.selectAllRank(offset,category,pageNum,pageSize);
       model.addAllAttributes(workRank.getList());
       model.addAttribute("category",category);
       model.addAttribute("pageNum",pageNum);
       model.addAttribute("pageSize",pageSize);
       model.addAttribute("offset",offset);
       return "publish-page";
    }



    @PostMapping("/add-work/{category}")
    @Operation(summary = "添加作品", description = "添加作品")
    public String add(@PathVariable("category") Integer category,
                      @RequestBody RankAddRequest rankAddRequestDto,
                      @RequestHeader("Authorization") String authHeader,
                      RedirectAttributes redirectAttributes) throws Exception {
        // 1. 修复：仅截取1次Bearer前缀（删除后续重复截取的错误代码）
        String token = authHeader.substring(7).trim();
        Integer userId = JwtUtils.getUserIdFromToken(token);

        // 修复：字符串判空用equals，避免==""失效
        if (rankAddRequestDto.getRankName() == null ||rankAddRequestDto.getRankName().isEmpty()) {
            // 修复：addFlashAttribute参数格式（属性名+逗号+提示内容）
            redirectAttributes.addFlashAttribute("errormessage", "榜单名不能为空");
            // 修复：重定向URL拼接（加?开头，&分隔参数）
            return "redirect:/music/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category;
        }

        List<RankAddRequest.RankSongItem> songItems = rankAddRequestDto.getSongItems();

        // 修复：先判空再遍历，避免空列表触发空指针
        if (songItems == null || songItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "歌曲榜单不能完全为空");
            return "redirect:/music/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category;
        }

        // 修复：排名校验先判空，避免null==0触发空指针
        for (RankAddRequest.RankSongItem item : songItems) {
            if (item.getSongId() == null || item.getSongId() <= 0 || item.getRanking() == null || item.getRanking() == 0) {
                redirectAttributes.addFlashAttribute("errormessage", "榜单的歌曲不能不存在或者其排名为空");
                return "redirect:/music/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category;
            }
        }

        // 修复：Token校验提示信息错误（匹配实际错误）
        if (token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "登录失效，请重新登录");
            return "redirect:/login.html";
        }

        try {
            userId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errormessage", "登录失效，请重新登录");
            return "redirect:/login.html";
        }

        // 核心入库逻辑（完全保留你原有代码，未修改）
        boolean result = workPublishServiceImpl.insertRank(category, userId, rankAddRequestDto);
        if (result) {
            // 修复：addFlashAttribute参数格式+URL拼接
            redirectAttributes.addFlashAttribute("success", "发布榜单成功！");
            return "publish-page";
        } else {
            // 修复：addFlashAttribute参数格式+URL拼接
            redirectAttributes.addFlashAttribute("errormessage", "发布失败，请稍后再试");
            return "publish-page";
        }
    }




    @GetMapping("/work")
    @ResponseBody
    @Operation(summary = "获取作品", description = "获取作品")
    //点击添加歌曲的按钮不涉及重定向，URL没有变化，页面也没有刷新
    public List<Work> work(@RequestParam("category") Integer categoryId){
        return workPublishServiceImpl.selectWork(categoryId);
    }




    @GetMapping("/song")
    @ResponseBody
    @Operation(summary = "获取歌曲", description = "获取歌曲")
    public List<Song>  song(@RequestParam("workId") Integer workId
    ){     return workPublishServiceImpl.selectSong(workId);
    }
    @PostMapping("/vote")
    @Operation(summary = "投票", description = "投票")
    public String vote(@RequestParam("token")String token, @RequestParam("rankId")Integer rankId,@RequestParam("category")Integer category,
                       @RequestParam("userId")Integer userId,@RequestParam("pageNum")Integer pageNum, @RequestParam("pageSize")Integer pageSize,
                       RedirectAttributes redirectAttributes, HttpServletRequest request) throws Exception {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage","token不能为空，请重新登录");
            return "redirect:/login.html";
        }

        try {
            userId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errormessage","userId不能为空，请重新登录");
            return "redirect:/login.html";
        }
        String ip = request.getRemoteAddr();
        boolean voteResult=workPublishServiceImpl.insertVote(userId,rankId,ip);
        if (voteResult) {
            redirectAttributes.addFlashAttribute("success","投票成功");
            return "redirect:/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category+"&pageNum=" + pageNum + "&pageSize=" + pageSize;
        }else{
            redirectAttributes.addAttribute("errormessage","投票失败，请稍后再试");
            return "redirect:/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category+"&pageNum=" + pageNum + "&pageSize=" + pageSize;
        }
    }




    @PostMapping("/love")
    @Operation(summary = "收藏", description = "收藏")
    public String love(@RequestParam("token")String token,@RequestParam("userId")Integer userId,@RequestParam("rankId")Integer rankId,
                       @RequestParam("category")Integer category,@RequestParam("pageNum")Integer pageNum, @RequestParam("pageSize")Integer pageSize,
                       RedirectAttributes redirectAttributes,HttpServletRequest request) throws Exception {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage","token不能为空，请重新登录");
            return "redirect:/login.html";
        }
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errormessage","userId不能为空，请重新登录");
            return "redirect:/login.html";
        }
        String ip = request.getRemoteAddr();
        boolean loveResult=workPublishServiceImpl.insertLove(userId,ip,rankId);
        if (loveResult) {
            redirectAttributes.addFlashAttribute("success","收藏成功");
            return "redirect:/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category+"&pageNum=" + pageNum + "&pageSize=" + pageSize;
        }else{
            redirectAttributes.addFlashAttribute("errormessage","收藏失败，请稍后再试");
            return "redirect:/api/rank/publish?token=" + token + "&userId=" + userId + "&category=" + category+"&pageNum=" + pageNum + "&pageSize=" + pageSize;
        }
    }




    @GetMapping("/sou")
    @Operation(summary = "搜索", description = "搜索")
    public String sou(@RequestParam("token")String token,@RequestParam("category")Integer category,
                      RedirectAttributes redirectAttributes) throws Exception {
        if (token == null || token.isEmpty()) {
            return  "redirect:/login.html";
        }
        Integer userId=JwtUtils.getUserIdFromToken(token);
        if (userId==null) {
            return  "redirect:/login.html";
        }
        if(category==null){
            return "redirect:/index.html";
        }
        redirectAttributes.addFlashAttribute("token", token);
        redirectAttributes.addFlashAttribute("category", category);
        return  "redirect:/api/rank/search?token=" + token + "&category=" + category+"&userId="+userId;
    }







    @GetMapping("/search")
    @Operation(summary = "搜索", description = "搜索")
    public String search(@RequestParam("token")String token,@RequestParam("category")Integer category,
                         @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                         @RequestParam(value = "pageSize",defaultValue = "3")Integer pageSize
            ,Model model,@RequestParam(value = "keyword",defaultValue = " ")String keyword ) throws Exception {
        //默认keyword为空，这样可以避免400
        Integer offset=(pageNum-1)*pageSize;
        Integer userId = JwtUtils.getUserIdFromToken(token);
        keyword=(keyword!=null)?keyword.trim():null;//对关键词做处理
        PageInfo<MyRankWithSong> searchRank = workPublishServiceImpl.selectSearch(category,pageNum,offset,pageSize,keyword);
        model.addAttribute("searchRank", searchRank);
        model.addAttribute("token", token);
        model.addAttribute("userId",userId);
        model.addAttribute("category",category);
        model.addAttribute("pageNum",pageNum);
        model.addAttribute("pageSize",pageSize);
        model.addAttribute("offset",offset);
        return "search-page";
    }





    @PostMapping("/pubcomment")
    @Operation(summary = "发表评论", description = "发表评论")
    @ResponseBody
    public Result<String> pubcomment(@RequestParam("token")String token, @RequestParam("rankId")Integer rankId, @RequestParam("userId")Integer userId,
                                     @RequestParam("content")String content, @RequestParam(value = "parentId",required = false)Integer parentId){

        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空，请重新登录");}
        if(rankId==null){
            return Result.error("rankId不能为空捏");
        }
        try{
            if(userId==null){
                return Result.error("当前用户不存在");
            }
            Comment comment=workPublishServiceImpl.insertComment(rankId,userId,content,parentId);
            return Result.success("评论发表成功"+comment);
        }catch (Exception e){
            return   Result.error("评论发表失败"+e.getMessage());
        }
    }





    @GetMapping("/list")
    @Operation(summary = "评论列表", description = "评论列表")
    @ResponseBody
    public Result<List<CommentVo>> list(@RequestParam("token")String token
            , @RequestParam("rankId")Integer rankId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            List<CommentVo> comment=workPublishServiceImpl.selectComment(rankId,userId);
            return Result.success(comment);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("评论展示失败");
        }
    }



    @DeleteMapping("deleteComment")
    @Operation(summary = "删除评论", description = "删除评论")
    @ResponseBody
    public Result<String> deleteComment(@RequestParam("token")String token,@RequestParam("comId")Integer comId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                return Result.error("comId不能为空");
            }
            boolean result=workPublishServiceImpl.deleteComment(comId,userId);
            if(result){
                return Result.success();
            }else{
                return Result.error("评论删除失败");
            }
        }catch (Exception e){
            return Result.error("评论删除失败");
        }
    }



    @PostMapping("/like")
    @Operation(summary = "点赞评论", description = "点赞评论")
    @ResponseBody
    public Result<String> like(@RequestParam("token")String token,@RequestParam("comId")Integer comId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                return Result.error("likeId不能为空");
            }
            boolean result=workPublishServiceImpl.insertLike(userId,comId);
            if(result){
                return Result.success();
            }else {
                return Result.error("点赞评论失败");
            }
        }catch (Exception e){
            return Result.error("点赞该评论失败"+e.getMessage());
        }
    }


    @PostMapping("deleteLike")
    @Operation(summary = "取消点赞", description = "取消点赞")
    @ResponseBody
    public Result<String> deleteLike(@RequestParam("token")String token,@RequestParam("comId")Integer comId){

        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                return Result.error("comId不能为空");
            }
            boolean result=workPublishServiceImpl.deleteLike(comId,userId);
            if(result){
                return Result.success("success");
            }else{
                return Result.error("取消点赞失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("取消点赞失败"+e.getMessage());
        }
    }
}

