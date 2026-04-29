package com.music.Service.impl;

import com.music.Mapper.UserInterestVetorMapper;
import com.music.Service.UserInterestVetorService;
import com.music.pojo.UserInterestVetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserInterestVetorServiceImpl implements UserInterestVetorService {
    @Autowired
    private UserInterestVetorMapper userInterestVetorMapper;
    @Override
    public List<UserInterestVetor> getUserInterestVetor(int userId) {
        if(userId==0){
            return Collections.emptyList();
        }else{
            return userInterestVetorMapper.selectUserInterestById(userId);
        }
    }
}
