package com.music.Service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.music.Mapper.MyRankMapper;
import com.music.Service.MyRankService;
import com.music.dto.MyRankWithSong;
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
    public PageInfo<MyRankWithSong> selectMyrank(Integer pageNum,Integer pageSize,Integer userId) {
        PageHelper.startPage(pageNum,pageSize);
      List<MyRankWithSong> personalRanks=myRankMapper.selectMyRank(userId);
          return new PageInfo<>(personalRanks);
    }
}
