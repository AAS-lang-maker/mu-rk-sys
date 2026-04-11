package com.music.Service;

import com.music.dto.LoginSuccessVo;
import com.music.dto.UserLoginDTO;
import com.music.dto.UserRegisterDTO;
import com.music.utils.Result;
import jakarta.validation.Valid;

public interface WebMasterLoginService {
    Result<LoginSuccessVo> selectWebmaster(@Valid UserLoginDTO userLoginDTO);

    Result<String> register(@Valid UserRegisterDTO userRegisterDTO);


}
