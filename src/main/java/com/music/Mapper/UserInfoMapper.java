package com.music.Mapper;

import com.music.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper {
    int insert(UserInfo userInfo);//注册
    UserInfo selectByUsername(String username);//登录

}
