package com.music.Controller;

import com.music.Service.HotRankService;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController//???
@RequestMapping("/api/hot")
public class MusicHotRankController {
    private static final Logger log = LoggerFactory.getLogger(UserPublishController.class);

    @Autowired
    private HotRankService hotRankService;
    //热门榜单整体逻辑写在纸上
    @GetMapping("/hotrank")
    public Result<List<MyRankWithSong>> getHotRank(@RequestParam(defaultValue = "0") Integer start, @RequestParam(defaultValue = "4")
                                     Integer end, @RequestParam("token")String token){
        if(token==null){
            return Result.error("token is null!....");
        }
        Set<String> idset = hotRankService.getHotRankId(start,end);
        if(idset.isEmpty()){
            return Result.error("该集合不能为空");
        }
        //通过流处理，将Set中的String转为Long类型rankId，最后把它整体转变为List集合
        //所以整体实现思路是redis的核心Service层，再到Mapper或者Controller层
        List<Long> rankId=idset.stream().map(Long::valueOf).collect(Collectors.toList());
        List<MyRankWithSong> list=hotRankService.listById(rankId);
        return Result.success(list);//前端要接受到查询回显的热门榜单集合
    }
    //为什么要这样做，因为Service层的数据全部为Long类型，且Mybatis中通过foreach循环List集合

    @PostMapping("/vote")
    @ResponseBody
    public Result<String> vote(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(rankId==null){
                return Result.error("likeId不能为空");
            }
            boolean result=hotRankService.insertVote(userId,rankId);
            if(result==true){
                return Result.success();
            }else {
                return Result.error("点赞榜单失败");
            }
        }catch (Exception e){
            return Result.error("点赞该榜单失败"+e.getMessage());
        }
    }



    @PostMapping("/love")
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
            boolean result=hotRankService.insertLove(userId,rankId);
            if(result==true){
                return Result.success();
            }else {
                return Result.error("点赞榜单失败");
            }
        }catch (Exception e){
            return Result.error("点赞该榜单失败"+e.getMessage());
        }
    }
    @Deprecated
    @PostMapping("deleteVote")
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
            boolean result=hotRankService.deleteVote(rankId,userId);
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
            boolean result=hotRankService.deleteLove(rankId,userId);
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
    @GetMapping("/list")
    @ResponseBody
    public Result<List<CommentVo>> list(@RequestParam("token")String token
            , @RequestParam("rankId")Integer rankId, @RequestParam("userId")Integer userId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            List<CommentVo> comment=hotRankService.selectComment(rankId,userId);
            return Result.success(comment);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("评论展示失败");
        }
    }
    @DeleteMapping("deleteComment")
    @ResponseBody
    public Result<String> deleteComment(@RequestParam("token")String token,@RequestParam("comId")Integer comId,
                                        @RequestParam("userId")Integer userId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空捏");
        }
        try{
            if(comId==null){
                return Result.error("comId不能为空");
            }
            boolean result=hotRankService.deleteComment(comId,userId);
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
    public Result<String> like(@RequestParam("token")String token,@RequestParam("comId")Integer comId){
        if(token == null || token.isEmpty()) {
            return Result.error("token不能为空哦");
        }
        try{
            Integer userId = JwtUtils.getUserIdFromToken(token);
            if(comId==null){
                return Result.error("likeId不能为空");
            }
            boolean result=hotRankService.insertLike(userId,comId);
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
            boolean result=hotRankService.deleteLike(comId,userId);
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
