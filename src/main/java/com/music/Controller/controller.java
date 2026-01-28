package com.music.Controller;

import com.music.Service.service;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
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
    public String login(@Validated UserLoginDTO userLoginDTO, Model model) {
        Result<String> result = Service.login(userLoginDTO);
        if (result.getCode() == 200) {
            return "redirect:/index.html";  //重定向回到首页
        } else
            model.addAttribute("errormessage", result.getMsg());//给前端传递错误信息
        String s1 = "forward:/login.html";
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
    public String register(@Validated UserRegisterDTO userRegisterDTO, Model model) {
        Result<String> result = Service.register(userRegisterDTO);//让Serrvice层校验用户名是否存在
        if (result.getCode() == 200) {
            return "redirect:/index.html";
        } else {
            String s2 = "该用户已经存在，请重新进行登录";
            model.addAttribute("errormessage", s2 + result.getMsg());
            return "redirect:/login.html";
        }
    }
}

