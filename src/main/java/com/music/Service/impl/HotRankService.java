package com.music.Service.impl;

import com.music.Mapper.MusicHotRankMapper;
import com.music.dto.MyRankWithSong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class HotRankService {
    //设置热门榜单Zset集合的键值
    //Redis核心写在Service层，配和Mapper层的Mysql查询联合完成热门榜单
    /*逻辑：    1.设置榜单排名的分数计算公式，自定义
               2.根据公式调用Redis，根据分数公式对分数进行排名并实时更新
                */
    private static final String Hot_Rank_Key="music:rank:hot";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private MusicHotRankMapper musicHotRankMapper;

    public Long caculateHotRank(Long rankId){
        Long love=musicHotRankMapper.CountLove(rankId);
        Long vote=musicHotRankMapper.CountVote(rankId);
        return love+2*vote;
    }

    //第二步的逻辑包括：将新建的Zset集合中添加榜单各项数据+查询榜单排名
    public void updateHotRank(Long rankId){
        if(rankId==null){
            return;
        }
        Long score=caculateHotRank(rankId);//此步相当于调用上方的自定义的计算分数的方法

        redisTemplate.opsForZSet().add(Hot_Rank_Key,score,rankId);
    }
    public Set<Object> getHotRankId(int topN){
         return redisTemplate.opsForZSet().reverseRange(Hot_Rank_Key,0,topN-1);
    }

    public List<MyRankWithSong> listById(List<Long> rankId) {
        return musicHotRankMapper.listByIds(rankId);
    }


}
