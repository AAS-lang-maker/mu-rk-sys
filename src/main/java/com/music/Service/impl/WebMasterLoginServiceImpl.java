package com.music.Service.impl;

import com.music.Mapper.WebMasterLoginMapper;
import com.music.Service.WebMasterLoginService;
import com.music.dto.LoginSuccessVo;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.pojo.UserInfo;
import com.music.utils.JwtUtils;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;


@Service
public class WebMasterLoginServiceImpl implements WebMasterLoginService {
    @Autowired
    private WebMasterLoginMapper webMasterLoginMapper;
    @Autowired  //怎么会有这么高级的注解。。密码加密器
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    public Result<LoginSuccessVo> selectWebmaster(UserLoginDTO userLoginDTO) {
        if(userLoginDTO.getUsername()==null||userLoginDTO.getPassword()==null){
            return Result.error("请输入用户名和密码");
        }
        UserInfo admin =webMasterLoginMapper.selectAdmin(userLoginDTO.getUsername());
       if(admin==null){
           return Result.error("不存在该管理员");
       }
       if(admin.getRole()!=1){
           return Result.error("您当前还不是管理员");
       }

       if(!(passwordEncoder.matches(userLoginDTO.getPassword(),admin.getPassword()))){
           return Result.error("密码错误");
       }
       String token= JwtUtils.generateToken(admin.getId(),admin.getUsername());
       LoginSuccessVo loginSuccessVo=new LoginSuccessVo();
       loginSuccessVo.setToken(token);
       loginSuccessVo.setUserId(String.valueOf(admin.getId()));
       loginSuccessVo.setUserName(admin.getUsername());
       return Result.success(loginSuccessVo);
    }

    @Override
    public Result<String> register(UserRegisterDTO userRegisterDTO) {
        if(userRegisterDTO==null|| StringUtils.isEmpty(userRegisterDTO.getUsername())
                ||StringUtils.isEmpty(userRegisterDTO.getPassword())){
            return Result.error("用户名或密码不能为空");
        }
        // 2. 入参去空格，避免前后端传参带空格的隐性问题   最麻烦的一集。
        String username = userRegisterDTO.getUsername().trim();
        String password = userRegisterDTO.getPassword().trim();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.error("用户名或密码不能为空格");
        }//二次校验没有空格，都怪页面老时500，把我当人机整
        if(username.length()<3||username.length()>16){
            return Result.error("用户名长度要在3-16位");
        }
        if(password.length()<6||password.length()>15){
            return Result.error("用户密码要在6-15位");
        }
        UserInfo user=  webMasterLoginMapper.selectAdmin(username);
        if(user!=null){
            return Result.error("当前用户存在，请登录");
        }
        UserInfo newUser=new UserInfo();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));//加密密码保存
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setRole(1);
        webMasterLoginMapper.insertAdmin(newUser);
        return Result.success();
    }
}
