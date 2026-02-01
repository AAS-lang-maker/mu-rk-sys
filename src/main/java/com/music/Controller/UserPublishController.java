package com.music.Controller;

import com.music.Service.UserPublishService;
import com.music.dto.RankAddRequest;
import com.music.pojo.UserInfo;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/api/rank")
public class UserPublishController {
    @Autowired
    private UserPublishService userPublishService;
    @PostMapping("/add")
    public String add(@PathVariable("categoryId") Integer categoryId, @RequestBody RankAddRequest rankAddRequestDto,
    HttpServletRequest request, RedirectAttributes redirectAttributes) {
        /*校验榜单名，用户登录状态，用户发布的歌曲不能为空或小于等于1首，分类id*/
        if(rankAddRequestDto.getRankName()==null||rankAddRequestDto.getRankName()==""){
            redirectAttributes.addFlashAttribute("errormessage"+"榜单名不能为空");
            return "redirect:/rank/add";
        }
        //想不到的点又降临了。。调用DTO类下的songDTO，并将其值传给集合，因为用户会上传很多歌曲及其对应排名
        List<RankAddRequest.RankSongItem> songItems = rankAddRequestDto.getSongItems();
        for(RankAddRequest.RankSongItem item:songItems){
            if(item.getSongId()==null||item.getRanking()==0){
                redirectAttributes.addFlashAttribute("errormessage"+"榜单的歌曲不能不存在或者其排名为空");
                return "redirect:/rank/add";
            }
        }
        if(songItems==null||songItems.isEmpty()){
            redirectAttributes.addFlashAttribute("errormessage"+"歌曲榜单不能完全为空");
            return "redirect:/rank/add";
        }
        //验证登录状态，基于Jwt令牌验证
        String token = request.getHeader("token");
        if(token==null||token==""){
            redirectAttributes.addFlashAttribute("errormessage"+"歌曲榜单不能完全为空");
            return "redirect:/rank/add";
        }
        token = token.substring(7);//高级东西又来了，去掉token的“Bearer”前缀。。。
        Integer userId;
         try{
             userId= JwtUtils.getUserIdFromTken(token);//从令牌中拿去用户的id，这里要在Jwt工具类中新建此方法
         }catch(Exception e){
             redirectAttributes.addFlashAttribute("errormessage"+"登录失效，请重新登录");
             return "redirect:/login.html";
         }
         //调用Service层
         boolean result=userPublishService.insertRank(categoryId,userId,rankAddRequestDto);
         if(result){
             redirectAttributes.addFlashAttribute("success"+"发布榜单成功！");
              return "redirect:/publish.html";
         }else{
             redirectAttributes.addFlashAttribute("errormessage"+"发布失败，请稍后再试");
             return "redirect:/rank/add";
         }
    }
}
