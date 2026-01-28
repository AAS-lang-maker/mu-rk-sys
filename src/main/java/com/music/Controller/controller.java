package com.music.Controller;

import com.music.Service.service;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/user")
@Validated  //对前端传来的参数实现分组校验
public class controller {
    @Autowired//注入service层
    private service Service;
    /*登录实现逻辑：
                1.接受前端传递的用户信息（注解）
                2.调用Service层
                3.接受前端返回的失败或成功的结果
                4.根据结果转发或者重定向*/

    @PostMapping("/login")
    public String login(@Validated UserLoginDTO userLoginDTO, RedirectAttributes redirectAttributes) {
        Result<String> result = Service.login(userLoginDTO);
        if (result.getCode() == 200) {
            redirectAttributes.addFlashAttribute("success", "登录成功，将为您跳转到首页");
            return "redirect:/music/index.html";  //重定向回到首页
        } else
            redirectAttributes.addAttribute("errormessage", result.getMsg());//给前端传递错误信息
        String s1 = "forward:/music/login.html";
        return s1; //转发返回登录页面
    }

    /*注册逻辑：
             1.接受前端出传递的注册用户信息
             2.看用户名是否在数据库中存在
             3.接受Service层返回的结果
             4.存在则重定向到登录界面并返回信息：您已经注册，请重新登录
             5.不存在则继续让用户输入密码
             6.注册完成后可直接重定向到首页*/
    @PostMapping("/register")
    public String register(@Validated UserRegisterDTO userRegisterDTO, RedirectAttributes  redirectAttributes) {
        //Model最没用的一集，由于在注册中要重定向，所以用RedirectAttributes传参数
        Result<String> result = Service.register(userRegisterDTO);//让Serrvice层校验用户名是否存在
        if (result.getCode() == 200) {
            redirectAttributes.addFlashAttribute("success", "注册成功，已为您跳转到首页");
            return "redirect:/music/index.html";
        } else {
            String s2 = "该用户已经存在，请重新进行登录";
            redirectAttributes.addAttribute("errormessage", s2 + result.getMsg());
            return "redirect:/login.html";
        }
    }
}

