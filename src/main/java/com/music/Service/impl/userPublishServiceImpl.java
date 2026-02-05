package com.music.Service.impl;

import com.github.pagehelper.PageInfo;
import com.music.Mapper.UserPublishMapper;
import com.music.Service.UserPublishService;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service

public class userPublishServiceImpl implements UserPublishService {
    @Autowired
    private UserPublishMapper userPublishMapper;

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)//事务注解:主表和子表的Service层操作必须同时成功，否则就是失败
    public boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) {
       //？依旧难想，将Dto类转化为对应数据库的实体类
        //先创建一个pojo对应的对象，再将前端dto的对应值传进去，最后调用Mapper层
        PersonalRank personalRank = new PersonalRank();
        personalRank.setCategoryId(categoryId);
        personalRank.setUserId(userId);
        //细心豆包，数据库里面默认target——id为空，所以当它真的为空，就设置其值为0
        personalRank.setTargetId(rankAddRequestDto.getTargetId() == null ? 0 : rankAddRequestDto.getTargetId());
        personalRank.setRankName(rankAddRequestDto.getRankName());
        userPublishMapper.insertRank(personalRank);
        //主表和子表的外键关联要拿出来（666）
        Integer rankId = personalRank.getRankId();
        System.out.println("获取的rankId：" + rankId);
        if (rankId == null) {
            System.out.println("rankId为null，直接返回失败"); // 新增：打印跳过逻辑
            return false;
        }

        List<RankSong> ranksongList=new ArrayList<RankSong>();//用集合接受前端榜单数据，因为歌曲和排名有很多，一个用户还可能有多个榜单
        //豆包大人教我写最难写的lamda？？表达式
        rankAddRequestDto.getSongItems().forEach(item->{
            RankSong rankSong=new RankSong();
            rankSong.setRankId(rankId);
            rankSong.setRanking(item.getRanking());
            rankSong.setSongId(item.getSongId());
            ranksongList.add(rankSong);
        });
        int insertCount = userPublishMapper.sixInsert(ranksongList);
        if (insertCount != ranksongList.size()) {
            System.out.println("子表插入行数与预期不符，返回失败");
            return false; // 触发事务回滚
        }
        return  true;
        }

    @Override
    public List<Singer> selectSinger(Integer categoryId) {
        List<Singer> singers=userPublishMapper.selectSinger(categoryId);
        return singers;
    }

    @Override
    public List<Song> selectSong(Integer singerId) {
        List<Song> songs=userPublishMapper.selectSong(singerId);
        return songs;
    }

    @Override
    public PageInfo<MyRankWithSong> selectPublishRank(Integer category, Integer pageNum, Integer pageSize, Integer offset) {
        List<MyRankWithSong> ranks=userPublishMapper.selectPublishRank(category,pageSize,offset);
        PageInfo<MyRankWithSong> pageInfo=new PageInfo();
        Integer total=userPublishMapper.selectTotal(category);
        Integer pages=(total+pageSize-1)/pageSize;
        System.out.println("所有榜单："+ranks.size());
        pageInfo.setList(ranks);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        return pageInfo;
    }

    @Override
    public boolean insertVote(Integer userId, Integer rankId, String ip) {
        int record=userPublishMapper.checkip(ip,rankId);//防刷票，匿名投票，利用ip检查
        if(record>0)
        { return false;}
        int rows=userPublishMapper.insertVote(rankId,ip);
        if(rows>0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean insertLove(Integer userId, String ip, Integer rankId) {
        int record1=userPublishMapper.checkip(ip,rankId);
        if(record1>0)
        { return false;}
        int row1=userPublishMapper.insertLove(userId,ip,rankId);
        if(row1>0){
            return true;
        }
        return false;
    }
}
