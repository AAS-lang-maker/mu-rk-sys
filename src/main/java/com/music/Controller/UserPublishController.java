package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.HotRankService;
import com.music.Service.UserPublishService;
import com.music.dto.ApplySongVo;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Comment;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import com.music.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


@Controller
@RequestMapping("/api/rank")
@Tag(name = "榜单管理", description = "榜单管理相关接口")
public class UserPublishController {
    private static final Logger log = LoggerFactory.getLogger(UserPublishController.class);
    @Resource
    private UserPublishService userPublishService;

    @Resource
    private HotRankService hotRankService;
    @GetMapping("/publish")
    @Operation(summary = "发布榜单", description = "发布榜单")
    public String publish(@RequestParam("token") String token, RedirectAttributes redirectAttributes, @RequestParam(value = "category",required = false) Integer category,
                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize, Model model) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "token已失效");
            return "redirect:/login.html";
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userId;
        try {
            userId = JwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "token不见啦，请重新登录");
            return "redirect:/login.html";
        }
        Integer offset = (pageNum - 1) * pageSize;
        PageInfo<MyRankWithSong> publishRanks = userPublishService.selectPublishRank(category, pageNum, pageSize, offset);
       model.addAttribute("rankList", publishRanks.getList());
       model.addAttribute("publishRanks", publishRanks);
       Song song=new Song();
       song.getSongId();
       model.addAttribute("song", song);
        model.addAttribute("category", category);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("offset", offset);
        return "publish-page";
    }

    @PostMapping("/add/{category}")
    @Operation(summary = "添加榜单", description = "添加榜单")
    @ResponseBody
    public Result<Integer> add(@PathVariable("category") Integer category,
                      @RequestBody RankAddRequest rankAddRequestDto,
                      HttpServletRequest request,
                      @RequestHeader("Authorization") String authHeader) throws Exception {
        Integer userId= ThreadLocalUtil.get();
        String token=(String) request.getAttribute("token");
        // 修复：字符串判空用equals，避免==""失效
        if (rankAddRequestDto.getRankName() == null || "".equals(rankAddRequestDto.getRankName())) {
           return Result.error("榜单名不能为空");
             }

        List<RankAddRequest.RankSongItem> songItems = rankAddRequestDto.getSongItems();

        // 修复：先判空再遍历，避免空列表触发空指针
        if (songItems == null || songItems.isEmpty()) {
          return Result.error("歌曲列表不能为空");
        }

        // 修复：排名校验先判空，避免null==0触发空指针
        for (RankAddRequest.RankSongItem item : songItems) {
            if (item.getSongId() == null || item.getSongId() <= 0 || item.getRanking() == null || item.getRanking() == 0) {
                   return Result.error("要选择歌曲和相应排名");   }
        }


        // 核心入库逻辑（完全保留你原有代码，未修改）
        Integer rankId = userPublishService.insertRank(category, userId, rankAddRequestDto);
        if (rankId != null && rankId > 0) {
            return Result.success(rankId);
         } else {
            return Result.error("榜单发布失败，请稍后再试一试");
           }
    }

    @GetMapping("/singer")
    @Operation(summary = "获取歌手", description = "获取歌手")
    @ResponseBody
    //点击添加歌曲的按钮不涉及重定向，URL没有变化，页面也没有刷新
    public List<Singer> singer(@RequestParam("token") String token, // 接收前端的token
                               @RequestParam("userId") Integer userId, // 接收前端的userId
                               @RequestParam("category") Integer category) {
        List<Singer> singers = userPublishService.selectSinger(category
        );
        return singers;
    }

    @GetMapping("/song")
    @Operation(summary = "获取歌曲", description = "获取歌曲")
    @ResponseBody
    public List<Song> song(@RequestParam("token") String token,
                           @RequestParam("userId") Integer userId,
                           @RequestParam("singerId") Integer singerId
    ) {
        List<Song> songs = userPublishService.selectSong(singerId);
        return songs;
    }

    @PostMapping("/applysong")
    @Operation(summary = "申请歌曲", description = "申请歌曲")
    @ResponseBody
    public Result<String> applySong(@RequestParam("token") String token, @RequestParam("songName")String songName,
                                    @RequestParam("singerName")String singerName){
        if (token == null || token.isEmpty()) {
            return Result.error("登录信息失效，请重新登录");
        }
  boolean falg= userPublishService.insertApply(singerName,songName);
    if (falg) {
        return Result.success("已收到您的请求，我们会尽快更新歌曲的~");
    }    else{
        return  Result.error("申请失败，请稍后再试试");
    }
    }

    @PostMapping("/vote")
    @ResponseBody
    @Operation(summary = "点赞榜单", description = "点赞榜单")
    public Result<String> vote(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(rankId==null){
                return Result.error("likeId不能为空");
            }
            // 和热门榜单接口保持同一套投票计分逻辑：
            // 统一走 HotRankService，确保计数、ZSet、排名战报一致
            boolean result=hotRankService.insertVote(userId,rankId);
            if(result==true){
                log.info("投票成功 ");
                hotRankService.updateHotRank(rankId);
                return Result.success("点赞成功，战报正在后台生成...");

            }else {
                return Result.error("点赞榜单失败");
            }
        }catch (Exception e){
            return Result.error("点赞该榜单失败"+e.getMessage());
        }
    }



    @PostMapping("/love")
    @Operation(summary = "收藏榜单", description = "收藏榜单")
    @ResponseBody
    public Result<String> love(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(rankId==null){
                return Result.error("likeId不能为空");
            }

            boolean result=userPublishService.insertLove(userId,rankId);
            if(result==true){
                return Result.success();
            }else {
                return Result.error("收藏榜单失败");
            }
        }catch (Exception e){
            return Result.error("收藏该榜单失败"+e.getMessage());
        }
    }
    @Deprecated
    @PostMapping("deleteVote")
    @Operation(summary = "取消点赞榜单", description = "取消点赞榜单")
    @ResponseBody
    public Result<String> deleteVote(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId){

        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(rankId==null){
                log.warn("【取消点赞】失败：rankId为空");
                return Result.error("rankId不能为空");
            }
            boolean result=userPublishService.deleteVote(rankId,userId);
            if(result==true){
                return Result.success("success");
            }else{
                return Result.error("取消点赞失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("取消点赞失败"+e.getMessage());
        }
    }
    @Operation(summary = "取消收藏榜单", description = "取消收藏榜单")
    @PostMapping("deleteLove")
    @ResponseBody
    public Result<String> deleteLove(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId){

        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(rankId==null){
                log.warn("【取消点赞】失败：rankId为空");
                return Result.error("rankId不能为空");
            }
            log.info("【取消点赞】删除条件：userId={}, rankId={}", userId, rankId);
            boolean result=userPublishService.deleteLove(rankId,userId);
            if(result==true){
                return Result.success("success");
            }else{
                return Result.error("取消点赞失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("取消点赞失败"+e.getMessage());
        }
    }
    @Operation(summary = "搜索榜单", description = "搜索榜单")
        @GetMapping("/sou")
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
    @Operation(summary = "搜索榜单", description = "搜索榜单")
        @GetMapping("/search")
    public String search(@RequestParam("token")String token,@RequestParam("category")Integer category,
                                           @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                           @RequestParam(value = "pageSize",defaultValue = "3")Integer pageSize
                                           ,Model model,@RequestParam(value = "keyword",defaultValue = " ")String keyword ) throws Exception {
        //默认keyword为空，这样可以避免400
        Integer offset=(pageNum-1)*pageSize;
        Integer userId = JwtUtils.getUserIdFromToken(token);
        keyword=(keyword!=null)?keyword.trim():null;//对关键词做处理
         PageInfo<MyRankWithSong> searchRank = userPublishService.selectSearch(category,pageNum,offset,pageSize,keyword);
         model.addAttribute("searchRank", searchRank);
         model.addAttribute("ranklist", searchRank.getList());
            Song song=new Song();
            song.getSongId();
            model.addAttribute("song", song);
         model.addAttribute("token", token);
         model.addAttribute("userId",userId);
         model.addAttribute("category",category);
         model.addAttribute("pageNum",pageNum);
         model.addAttribute("pageSize",pageSize);
         model.addAttribute("offset",offset);
         return "search-page";
        }
      @Operation(summary = "发表评论", description = "发表评论")
      @PostMapping("/pubcomment")
      @ResponseBody
    public Result<String> pubcomment(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId,@RequestParam("userId")Integer userId,
                                     @RequestParam("content")String content,@RequestParam(value = "parentId",required = false)Integer parentId){

      if(token == null || token.isEmpty()) {
      return Result.error("token不能为空，请重新登录");}
      if(rankId==null){
          return Result.error("rankId不能为空捏");
      }
      try{
          if(userId==null){
              return Result.error("当前用户不存在");
          }
          Comment comment=userPublishService.insertComment(rankId,userId,content,parentId);
          return Result.success("评论发表成功"+comment);
      }catch (Exception e){
          return   Result.error("评论发表失败"+e.getMessage());
      }
      }
      @Operation(summary = "获取评论列表", description = "获取评论列表")
      @GetMapping("/list")
      @ResponseBody
    public Result<List<CommentVo>> list(@RequestParam("token")String token
                               , @RequestParam("rankId")Integer rankId,@RequestParam("userId")Integer userId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            List<CommentVo> comment=userPublishService.selectComment(rankId,userId);
            return Result.success(comment);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("评论展示失败");
        }
      }
      @DeleteMapping("deleteComment")
      @ResponseBody
      @Operation(summary = "删除评论", description = "删除评论")
    public Result<String> deleteComment(@RequestParam("token")String token,@RequestParam("comId")Integer comId,
                                        @RequestParam("userId")Integer userId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            if(comId==null){
                return Result.error("comId不能为空");
            }
            boolean result=userPublishService.deleteComment(comId,userId);
            if(result==true){
                return Result.success();
            }else{
                return Result.error("评论删除失败");
            }
        }catch (Exception e){
            return Result.error("评论删除失败");
        }
      }
      @PostMapping("/like")
      @ResponseBody
      @Operation(summary = "点赞评论", description = "点赞评论")
    public Result<String> like(@RequestParam("token")String token,@RequestParam("comId")Integer comId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                return Result.error("likeId不能为空");
            }
            boolean result=userPublishService.insertLike(userId,comId);
            if(result==true){
                return Result.success();
            }else {
                return Result.error("点赞评论失败");
            }
        }catch (Exception e){
            return Result.error("点赞该评论失败"+e.getMessage());
        }
      }
      @PostMapping("deleteLike")
      @ResponseBody
      @Operation(summary = "取消点赞评论", description = "取消点赞评论")
    public Result<String> deleteLike(@RequestParam("token")String token,@RequestParam("comId")Integer comId){

        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                log.warn("【取消点赞】失败：comId为空");
                return Result.error("comId不能为空");
            }
            boolean result=userPublishService.deleteLike(comId,userId);
            if(result==true){
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



