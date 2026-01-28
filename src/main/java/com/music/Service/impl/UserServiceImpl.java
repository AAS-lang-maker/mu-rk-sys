package com.music.Service.impl;

import com.music.Service.service;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements service {

    @Override
    public Result<String> login(UserLoginDTO userLoginDTO) {
        return null;
    }

    @Override
    public Result<String> register(UserRegisterDTO userRegisterDTO) {
        return null;
    }
}