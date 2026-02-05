package com.music.Service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.music.Mapper.MyRankMapper;
import com.music.Service.MyRankService;
import com.music.dto.EditRank;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class MyRankServiceImpl implements MyRankService {
@Autowired
private MyRankMapper myRankMapper;

    //查询personl_rank主表
    @Override
    public PageInfo<MyRankWithSong> selectMyrank(Integer pageNum,Integer pageSize,Integer offset,Integer userId) {
      List<MyRankWithSong> list =myRankMapper.selectMyRank(userId,pageSize,offset);
      System.out.println("Mapper查询到的榜单数："+list.size());
      Integer total = myRankMapper.selectMyRankTotal(userId);
      Integer pages=(total+pageSize-1)/pageSize;
       PageInfo<MyRankWithSong> pageInfo=new PageInfo<>();
       pageInfo.setList(list);
       pageInfo.setPageNum(pageNum);
       pageInfo.setPageSize(pageSize);
       pageInfo.setTotal(total);
       pageInfo.setPages(pages);
          return pageInfo;
    }

    @Override
    public MyRankWithSong getRank(Integer rankId) {
        return myRankMapper.getRank(rankId);
    }

    @Override
    public List<Singer> selectSinger(Integer categoryId) {
        List<Singer> singers=myRankMapper.selectSinger(categoryId);
         return singers;
    }

    @Override
    public List<Song> selectSong(Integer singerId) {
        List<Song> songs=myRankMapper.selectSong(singerId);
        return songs;
    }

    @Override
    @Transactional
    public void insertNewRank(EditRank dto) {
         Integer rankId=dto.getRankId();
         String rankName=dto.getRankName();
         myRankMapper.insertNewRankname(rankId,rankName);
         myRankMapper.deleteOldRankname(rankId);
         List<RankAddRequest.RankSongItem> items=dto.getSongItems();
        List<RankSong> newSongs=new ArrayList<>();
         if(!CollectionUtils.isEmpty(items)){
             for (RankAddRequest.RankSongItem item : items) {
                 RankSong newSong=new RankSong();
                 newSong.setSongId(item.getSongId());
                 newSong.setRanking(item.getRanking());
                 newSong.setRankId(rankId);
                 newSongs.add(newSong);
             }
         }
         if(!CollectionUtils.isEmpty(newSongs)){
         myRankMapper.insertNewRank(newSongs);
       }
    }
}
