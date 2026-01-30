package com.music.Mapper;

import com.music.pojo.Category;
import com.music.pojo.UserInfo;
import com.music.utils.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Locale;

@Mapper
public interface UserInfoMapper {
    int insert(UserInfo userInfo);//注册
    UserInfo selectByUsername(String username);//登录
}
