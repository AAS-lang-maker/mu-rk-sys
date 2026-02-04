package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.UserPublishService;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.pojo.UserInfo;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/rank")
public class UserPublishController {
    @Resource
    private UserPublishService userPublishService;
    @GetMapping("/publish")
    public String publish(@RequestParam("token")String token, @RequestParam("category")Integer category,
                          RedirectAttributes redirectAttributes,@RequestParam("pageNum")Integer pageNum,
                          @RequestParam("pageSize")Integer pageSize,Model model) {
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
       Integer offset = (pageNum-1)*pageSize;
        PageInfo<MyRankWithSong> publishRanks=userPublishService.selectPublishRank(category,pageNum,pageSize,offset);
        System.out.println("所有榜单是这样："+publishRanks.getList());
        model.addAllAttributes(publishRanks.getList());
        model.addAttribute("category",category);
        model.addAttribute("pageNum",pageNum);
        model.addAttribute("pageSize",pageSize);
        model.addAttribute("offset",offset);
        return "publish-page";
    }
    @PostMapping("/add/{categoryId}")
    public String add(@PathVariable("categoryId") Integer categoryId,
                      @RequestBody RankAddRequest rankAddRequestDto,
                      @RequestHeader("Authorization") String authHeader,
                      RedirectAttributes redirectAttributes) throws Exception {
        // 1. 修复：仅截取1次Bearer前缀（删除后续重复截取的错误代码）
        String token = authHeader.substring(7).trim();
        Integer userId = JwtUtils.getUserIdFromToken(token);

        // 修复：字符串判空用equals，避免==""失效
        if (rankAddRequestDto.getRankName() == null || "".equals(rankAddRequestDto.getRankName())) {
            // 修复：addFlashAttribute参数格式（属性名+逗号+提示内容）
            redirectAttributes.addFlashAttribute("errormessage", "榜单名不能为空");
            // 修复：重定向URL拼接（加?开头，&分隔参数）
            return "redirect:/publish.html?token=" + token + "&userId=" + userId + "&categoryId=" + categoryId;
        }

        List<RankAddRequest.RankSongItem> songItems = rankAddRequestDto.getSongItems();

        // 修复：先判空再遍历，避免空列表触发空指针
        if (songItems == null || songItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "歌曲榜单不能完全为空");
            return "redirect:/publish.html?token=" + token + "&userId=" + userId + "&categoryId=" + categoryId;
        }

        // 修复：排名校验先判空，避免null==0触发空指针
        for (RankAddRequest.RankSongItem item : songItems) {
            if (item.getSongId() == null || item.getSongId() <= 0 || item.getRanking() == null || item.getRanking() == 0) {
                redirectAttributes.addFlashAttribute("errormessage", "榜单的歌曲不能不存在或者其排名为空");
                return "redirect:/publish.html?token=" + token + "&userId=" + userId + "&categoryId=" + categoryId;
            }
        }

        // 修复：Token校验提示信息错误（匹配实际错误）
        if (token == null || "".equals(token)) {
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
        boolean result = userPublishService.insertRank(categoryId, userId, rankAddRequestDto);
        if (result) {
            // 修复：addFlashAttribute参数格式+URL拼接
            redirectAttributes.addFlashAttribute("success", "发布榜单成功！");
            return "redirect:/publish.html?token=" + token + "&userId=" + userId + "&categoryId=" + categoryId;
        } else {
            // 修复：addFlashAttribute参数格式+URL拼接
            redirectAttributes.addFlashAttribute("errormessage", "发布失败，请稍后再试");
            return "redirect:/publish.html?token=" + token + "&userId=" + userId + "&categoryId=" + categoryId;
        }
    }
    @GetMapping("/singer")
    @ResponseBody
    //点击添加歌曲的按钮不涉及重定向，URL没有变化，页面也没有刷新
    public List<Singer> singer(    @RequestParam("token") String token, // 接收前端的token
                                   @RequestParam("userId") Integer userId, // 接收前端的userId
                                   @RequestParam("categoryId") Integer categoryId){
        List<Singer> singers=userPublishService.selectSinger(categoryId);
        return singers;
    }
    @GetMapping("/song")
    @ResponseBody
    public List<Song>  song( @RequestParam("token") String token,
                             @RequestParam("userId") Integer userId,
                             @RequestParam("singerId") Integer singerId
                            ){
        List<Song> songs=userPublishService.selectSong(singerId);
        return songs;
    }
    @PostMapping("/vote")
    public String vote(@RequestParam("token")String token,@RequestParam("rankId")Integer rankId,
                       RedirectAttributes redirectAttributes,HttpServletRequest request) throws Exception {
        if (token == null || token.isEmpty()) {
           redirectAttributes.addFlashAttribute("errormessage","token不能为空，请重新登录");
           return "redirect:/login.html";
        }
        Integer userId= null;
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
        boolean voteResult=userPublishService.insertVote(userId,rankId,ip);
        if (voteResult) {
            redirectAttributes.addFlashAttribute("success","投票成功");
            return "publish-page";
        }else{
            redirectAttributes.addAttribute("errormessage","投票失败，请稍后再试");
            return "publish-page";
        }
    }
}
