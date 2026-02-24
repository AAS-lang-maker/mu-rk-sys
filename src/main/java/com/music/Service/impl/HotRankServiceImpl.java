package com.music.Service.impl;

import com.music.Mapper.MusicHotRankMapper;
import com.music.Service.HotRankService;
import com.music.dto.MyRankWithSong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class HotRankServiceImpl implements HotRankService {
    //设置热门榜单Zset集合的键值
    //Redis核心写在Service层，配和Mapper层的Mysql查询联合完成热门榜单
    /*逻辑：    1.设置榜单排名的分数计算公式，自定义
               2.根据公式调用Redis，根据分数公式对分数进行排名并实时更新
                */
    private static final String Hot_Rank_Key="music:rank:hot";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MusicHotRankMapper musicHotRankMapper;
    @Override
    public Long caculateHotRank(Long rankId){
        Long love=musicHotRankMapper.CountLove(rankId);
        Long vote=musicHotRankMapper.CountVote(rankId);
        //防止空指针？
        love=love==null?0L:love;
        vote=vote==null?0L:vote;
        return love+2*vote;
    }

    @Override
    //第二步的逻辑包括：将新建的Zset集合中添加榜单各项数据+查询榜单排名
    public void updateHotRank(Long rankId) {
        if(rankId==null){
            //直接抛出空指针异常
            throw new IllegalArgumentException("rankId不能为空");
        }
        System.out.println("===== 开始写入Redis =====");
        System.out.println("Key：" + Hot_Rank_Key);
        System.out.println("要写入的rankId：" + rankId);
        Long score=caculateHotRank(rankId);//此步相当于调用上方的自定义的计算分数的方法
        System.out.println("计算的热度值：" + score);
        //为什么toString？？？
        stringRedisTemplate.opsForZSet().add(Hot_Rank_Key,rankId.toString(),score);
        Set<String> afterWrite = stringRedisTemplate.opsForZSet().range(Hot_Rank_Key, 0, -1);
        System.out.println("写入后Redis中的数据：" + afterWrite);
        System.out.println("===== 写入Redis结束 =====");
    }

    @Override
    public Set<String> getHotRankId(int start,int end) {
        if(end<=0||start<0){
            return Collections.emptySet();//妙SOS,返回空集合
        }
         return stringRedisTemplate.opsForZSet().reverseRange(Hot_Rank_Key,start,end);
    }

    @Override
    public List<MyRankWithSong> listById(List<Long> rankId) {
        return musicHotRankMapper.listByIds(rankId);
    }


}
