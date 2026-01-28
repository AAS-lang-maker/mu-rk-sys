package com.music.Service;

import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;

public interface service {
    Result<String> login(UserLoginDTO userLoginDTO);

    Result<String> register(UserRegisterDTO userRegisterDTO);
}
