package com.music.Controller;

import com.music.Service.UserInfoService;
import com.music.dto.LoginSuccessVo;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
@Validated  //对前端传来的参数实现分组校验
public class Usercontroller {
    @Autowired//注入service层
    private UserInfoService userInfoService;
    /*登录实现逻辑：
                1.接受前端传递的用户信息（注解）
                2.调用Service层
                3.接受前端返回的失败或成功的结果
                4.根据结果转发或者重定向*/

    @PostMapping("/login")
    @ResponseBody
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginSuccessVo> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
       return userInfoService.login(userLoginDTO);
    }

    /*注册逻辑：
             1.接受前端出传递的注册用户信息
             2.看用户名是否在数据库中存在
             3.接受Service层返回的结果
             4.存在则重定向到登录界面并返回信息：您已经注册，请重新登录
             5.不存在则继续让用户输入密码
             6.注册完成后可直接重定向到首页*/
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public String register(@Validated UserRegisterDTO userRegisterDTO, RedirectAttributes redirectAttributes) {
        //Model最没用的一集，由于在注册中要重定向，所以用RedirectAttributes传参数
        Result<String> result = userInfoService.register(userRegisterDTO);//让Service层校验用户名是否存在
        if (result.getCode() == 200) {

            redirectAttributes.addFlashAttribute("success", "注册成功，已为您跳转到首页");
            return "redirect:/login.html";
        } else {
            String s2 = "该用户已经存在，请重新进行登录";
            redirectAttributes.addFlashAttribute("errormessage", s2 + result.getMsg());
            return "redirect:/login.html";
        }
    }
}
    // 这是一个专门给 Apifox/前后端分离用的注册接口
// 路径加了 /api 前缀，和原来的区分开
/*    @PostMapping("/register")
    @ResponseBody // 关键：告诉Spring Boot，我要返回JSON，不要跳页面！
    public Result<String> registerApi(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        // 直接调用Service，逻辑和原来一模一样
        Result<String> result = userInfoService.register(userRegisterDTO);

        // 直接返回 JSON 结果
        // 2. 根据结果直接返回 JSON 数据
        if (result.getCode() == 200) {
            // 注册成功，返回成功的 JSON
            return Result.success("注册成功，请登录");
        } else {
            // 注册失败，返回错误的 JSON
            return Result.error(result.getMsg());
        }
    }
    }
*/



