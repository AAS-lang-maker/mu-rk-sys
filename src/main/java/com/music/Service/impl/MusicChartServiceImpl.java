package com.music.Service.impl;

import com.music.Mapper.MusicChartMapper;
import com.music.Service.HotRankService;
import com.music.Service.MusicChartService;
import com.music.Service.UserInterestVetorService;
import com.music.dto.MyRankWithSong;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
@Slf4j
@Service
public class MusicChartServiceImpl implements MusicChartService {
    @Autowired
    private MusicChartMapper musicChartMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private HotRankService hotRankService;
    @Override
    @Tool("根据用户的个人榜单，以及听过的榜单。结合用户喜欢的风格，流派来推荐音乐榜单")
    public List<MyRankWithSong> getMusicChart(@P("去数据库查询的关键词，如风格，歌手") String keyword,
                                              @P("用户的1唯一身份userId") int  userId) {
        if(StringUtils.isBlank(keyword)){
            return Collections.emptyList();
        }else{
            log.info("准备模糊查询数据库相关歌曲");
           return musicChartMapper.selectChart(keyword.trim(),userId);
        }
    }
    @Override
    @Tool("根据目前的热门榜单，推荐给用户一些好的音乐榜单")
    public String getHotChart() {
        log.info("准备调用hotRankService方法");
        Set<String> hotranks=hotRankService.getHotRankId(0,9);
        log.info("调用成功");
        if(hotranks.isEmpty()){
            log.warn("没有查询到热门榜单");
            return "暂时没有查询到热门榜单";
        }else{
            log.info("热门榜单查询成功");
            return "热门榜单如下"+hotranks.toString();
        }
    }
}
