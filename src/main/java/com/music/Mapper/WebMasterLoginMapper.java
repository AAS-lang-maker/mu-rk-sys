package com.music.Mapper;

import com.music.dto.UserLoginDTO;
import com.music.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WebMasterLoginMapper {
    UserInfo selectAdmin(String username);

    void insertAdmin(UserInfo newUser);
}
