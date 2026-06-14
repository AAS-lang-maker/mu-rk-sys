package com.music.Controller;

import com.music.Service.WebMasterLoginService;
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
@RequestMapping("/api/role")
@Validated
@Tag(name = "WebMasterLoginController", description = "网站管理员登录控制器")
public class WebMasterLoginController {
    @Autowired
    private WebMasterLoginService webMasterLoginService;
    @PostMapping("/login")
    @ResponseBody
    @Operation(summary = "网站管理员登录", description = "网站管理员登录")
    public Result<LoginSuccessVo> admin(@Valid@RequestBody UserLoginDTO userLoginDTO) {
        Result<LoginSuccessVo> result=webMasterLoginService.selectWebmaster(userLoginDTO);
        return result;
    }
    @PostMapping("/register")
    @Operation(summary = "网站管理员注册", description = "网站管理员注册")
    public String adminRegister(@Valid UserRegisterDTO userRegisterDTO, RedirectAttributes
                                redirectAttributes) {
       Result<String> result =webMasterLoginService.register(userRegisterDTO);
        if(result.getCode()==200){
            redirectAttributes.addFlashAttribute("success","注册成功，请去登录吧");
            return "redirect:/login.html";
        }else{
            redirectAttributes.addFlashAttribute("errormessage","该用户已经注册过呢，请重新注册"+result.getMsg());
            return "redirect:/login.html";
        }
    }
}
