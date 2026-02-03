package com.music.Controller;

import com.github.pagehelper.PageInfo;
import com.music.Service.MyRankService;
import com.music.dto.MyRankWithSong;
import com.music.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//数据渲染（查询回显）和页面重定向跳转分开写
@Controller
@RequestMapping("/api/mine")
public class MyRankController {
    @Autowired
    private MyRankService myRankService;


    @GetMapping("/my")//没有调用Service，只负责页面跳转，没有要操作数据库的逻辑
    public String myRank(@RequestParam("token")String token,HttpServletRequest request, RedirectAttributes redirectAttributes){

    Integer userId=validateToken(token,redirectAttributes);
    if(userId==null){
        return "redirect:/login";
    }

       redirectAttributes.addFlashAttribute("userId", userId);
       redirectAttributes.addFlashAttribute("token", token);
        return "redirect:/api/mine/myrank";
    }

    @GetMapping("/myrank")
    //可以保证在个人榜单中心页面刷新的时候数据不丢失，因为每次刷新都是在调用上面的三层架构重新查询后再次重定向
    //在此页面需要重新校验token以及userId
    //pageNum 页码数，pageSize一个页面的展示数
    public String myrank(@RequestParam(value = "token",required = false)String token,
                         @RequestParam("userId")Integer paramuserId,RedirectAttributes redirectAttributes,Model model,
                         @RequestParam(defaultValue="1")Integer pageNum, @RequestParam(defaultValue = "5")Integer pageSize){//userId要随着重定向和转发跟随到下一个新页面

       Integer userId=validateToken(token,redirectAttributes);
       if(validateToken(token,redirectAttributes)==null){
           return "redirect:/login.html";
       }
       if(!userId.equals(paramuserId)){
           redirectAttributes.addFlashAttribute("errormessage","无权访问他人数据");
           return "redirect:/login.html";
       }

        PageInfo<MyRankWithSong> personalRanks=myRankService.selectMyrank(pageNum,pageSize,userId);
        model.addAttribute("personalRanks",personalRanks);//把这两个必要数据传进URL而不是一次性的Flash，重定向的时候数据也进去了
        model.addAttribute("userId", userId);
        return "myrank—page";
    }
    private Integer validateToken(String token,RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errormessage", "token已失效");
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userId;
        try {
            userId = JwtUtils.getUserIdFromToken(token);
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "token不见啦，请重新登录");
            return null;
        }
    }
}



















