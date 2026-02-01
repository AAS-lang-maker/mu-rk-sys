package com.music.Controller;

import com.music.Service.MyRankService;
import com.music.dto.MyRankWithSong;
import com.music.pojo.personalRank;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.apache.ibatis.ognl.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String myRank(  HttpServletRequest request, RedirectAttributes redirectAttributes){
        String token;
        token = request.getHeader("token");
        if(token==null||token.isEmpty())
        {
            redirectAttributes.addFlashAttribute("errormessage","token已失效");
            return "redirect:/api/user/login";
        }
        Integer userId;
        if(token.startsWith("Bearer "))
        {token=token.substring(7);}
        try
        {
            userId= JwtUtils.getUserIdFromTken(token);
        }catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage","token不见啦，请重新登录");
            return "redirect:/api/user/login";
        }
       redirectAttributes.addFlashAttribute("userId", userId);
        return "redirect:/api/user/myrank";
    }

    @GetMapping("/myrank")
    //可以保证在个人榜单中心页面刷新的时候数据不丢失，因为每次刷新都是在调用上面的三层架构重新查询后再次重定向
    //在此页面需要重新校验token以及userId
    public String myrank(@RequestParam(value = "userId",required = false)Integer urluserId,HttpServletRequest request,RedirectAttributes redirectAttributes, Model model){//userId要随着重定向和转发跟随到下一个新页面
        String token;//和上面1的校验一摸一样
        token = request.getHeader("token");
        if(token==null||token.isEmpty())
        {
            redirectAttributes.addFlashAttribute("errormessage","token已失效");
            return "redirect:/api/user/login";
        }
        if(token.startsWith("Bearer "))
        {token=token.substring(7);}
        Integer reaurluserId;
        try
        {
            reaurluserId= JwtUtils.getUserIdFromTken(token);
        }catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage","token不见啦，请重新登录");
            return "redirect:/api/user/login";
        }
        if(urluserId==null||urluserId.equals(reaurluserId)){
            return "redirect:/api/user/login";
        }

        List<MyRankWithSong> personalRanks=myRankService.selectMyrank(urluserId);
        model.addAttribute("personalRanks",personalRanks);//把这两个必要数据传进URL而不是一次性的Flash，重定向的时候数据也进去了
        model.addAttribute("userId", urluserId);
        return "myrank—page";
    }
}


















