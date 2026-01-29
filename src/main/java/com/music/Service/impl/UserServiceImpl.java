package com.music.Service.impl;


import com.music.Mapper.UserInfoMapper;
import com.music.Service.service;
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
public class UserServiceImpl implements service {
    @Autowired
     private UserInfoMapper userInfoMapper;
    @Autowired  //怎么会有这么高级的注解。。密码加密器
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    /*登录Service思路逻辑：
                      1.接受Controller层的登录用户信息,校验，防止其为空
                      2.调用mapper层,校验用户名是否存在
                      3.校验前端的明文密码和数据库中的加密密码是否一致
                      4.给Controller层返回响应成功或失败结果
                      5.如果登录成功还要生成token令牌给Controller*/
    public Result<String> login(UserLoginDTO userLoginDTO) {
        if(userLoginDTO==null|| StringUtils.isEmpty(userLoginDTO.getUsername())
                ||StringUtils.isEmpty(userLoginDTO.getPassword())){
            return Result.error("用户名或密码不能为空");
        }
        //调用Mapper层，Mapper中的本质是查询用户信息，而Service层要去解释Mapper的查询数据来校验
        //查询的有username+password，可以将其封装在一个实体类，即user类中（封装性）
        UserInfo user=  userInfoMapper.selectByUsername(userLoginDTO.getUsername());
        //强制类型转化为User，不然生成的Token令牌始终就是错的，何意味？
        if(user==null){
            return Result.error("用户名不存在，请先注册");
        }
        if(!passwordEncoder.matches(userLoginDTO.getPassword(),user.getPassword()))
        {
            return Result.error("密码错误");
        }
        // 用user.getId()作为Token的用户标识（如果你的实体类属性是id）
        String token = JwtUtils.generateToken(user.getId(),user.getUsername());
        //token工具类里面方法的参数对应，上面句子也是卡了我1个小时，getId一直爆红。何意味？
        Result<String> res=Result.success(token);//Result类里面new过了，现在只是要获取成功的结果
        res.setMsg("登录成功");//设置返回结果成功的信息，而不是get获取信息
        return res;
    }
    /*注册思路逻辑：
                1.获取前端传递的用户信息
                2.校验用户名不是空
                3.不是空的前提下对用户名和密码做出限定
                4.调用Mapper层，如果用户民存在就返回登陆页面，给Controller层返回错误结果
                5.用户名存在就继续将密码，用户名存入数据库中,调用mapper层(完善注册时间)，给Controller层返回success*/
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
        if(userRegisterDTO.getUsername().length()<3||userRegisterDTO.getUsername().length()>16){
            return Result.error("用户名长度要在3-16位");
        }
        if(userRegisterDTO.getPassword().length()<6||userRegisterDTO.getPassword().length()>15){
            return Result.error("用户密码要在6-15位");
        }
        UserInfo user=  userInfoMapper.selectByUsername(userRegisterDTO.getUsername());
        if(user!=null){
            return Result.error("当前用户存在，请登录");
        }
        //执行到此处，用户不存在，新建用户对象并封装其用户名和密码，调用mapper层新增一条数据
        UserInfo newUser=new UserInfo();
        newUser.setUsername(userRegisterDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));//加密密码保存
        //补充注册时间
        newUser.setCreateTime(LocalDateTime.now());
        /*豆包唯一权威的一次，让我用try-catch去调用mapper层，这样可以在出现：数据库连接失败，数据库约束冲突等一系列问题
        时可以看到异常的提示信息，提醒用户“注册失败。。。。。”*/

        //try-catch的代码是依托屎。
        try{
            int insertRows=userInfoMapper.insert(newUser);
             //insert返回值设置为整数，用于接住异常
            if(insertRows!=1){
                System.out.println("注册失败:用户"+username+"插入数据库失败");//屎
                return Result.error("注册失败，请稍后再试");
            }
        } catch (Exception e) {
            System.err.println("注册失败：用户" + username + "插入数据库异常");//屎
            e.printStackTrace();
            return Result.error("注册失败，请稍后重试"+e.getMessage());
        }
        return Result.success();
    }
}