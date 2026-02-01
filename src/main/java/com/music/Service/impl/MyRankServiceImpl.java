package com.music.Service.impl;

import com.music.Mapper.MyRankMapper;
import com.music.Service.MyRankService;
import com.music.dto.MyRankWithSong;
import com.music.pojo.personalRank;
import com.music.pojo.rankSong;
import com.music.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
public class MyRankServiceImpl implements MyRankService {
@Autowired
private MyRankMapper myRankMapper;

    //查询personl_rank主表
    @Override
    public List<MyRankWithSong> selectMyrank(Integer urluserId) {
      List<MyRankWithSong> personalRanks=myRankMapper.selectMyRank();
      if(CollectionUtils.isEmpty(personalRanks)){
          return Collections.emptyList();
    }else{
          return personalRanks;
      }
    }
}
