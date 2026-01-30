package com.music.Service.impl;

import com.music.Mapper.UserPublishMapper;
import com.music.Service.UserPublishService;
import com.music.dto.RankAddRequest;
import com.music.pojo.personalRank;
import com.music.pojo.rankSong;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional//事务注解:主表和子表的Service层操作必须同时成功，否则就是失败
public class userPublishServiceImpl implements UserPublishService {
    @Autowired
    private UserPublishMapper userPublishMapper;

    @Override
    public boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) {
       //？依旧难想，将Dto类转化为对应数据库的实体类
        //先创建一个pojo对应的对象，再将前端dto的对应值传进去，最后调用Mapper层
        personalRank personalRank = new personalRank();
        personalRank.setCategoryId(categoryId);
        personalRank.setUserId(userId);
        personalRank.setTargetId(rankAddRequestDto.getTargetId());
        personalRank.setRankName(rankAddRequestDto.getRankName());
        userPublishMapper.insertRank(personalRank);
        //主表和子表的外键关联要拿出来（666）
        List<rankSong> ranksongList=new ArrayList<rankSong>();//用集合接受前端榜单数据，因为歌曲和排名有很多，一个用户还可能有多个榜单
        //豆包大人教我写最难写的lamda？？表达式
        rankAddRequestDto.getSongItems().forEach(item->{
            rankSong rankSong=new rankSong();
            rankSong.setRankId(item.getRanking());
            rankSong.setSongId(item.getSongId());
            ranksongList.add(rankSong);
        });
        userPublishMapper.sixInsert(ranksongList);
        return  true;
        }
    }
