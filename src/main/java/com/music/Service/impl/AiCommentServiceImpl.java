package com.music.Service.impl;

import com.music.Service.AiCommentService;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.*;

@Service
@Slf4j
public class AiCommentServiceImpl implements AiCommentService {
    @Value("${siliconflow.api-key}")
    private String apiKey;
    private final static String MODEL="deepseek-ai/Deepseek-V3";
    private final static String API_URL="https://api.siliconflow.cn/v1/chat/completions";
    @Override
    public String getAiComment(RankSong rankSong, PersonalRank personalRank, RankTags rankTags) {
     log.info("AI锐评启动，正在分析用户喜欢的歌曲：{}",personalRank.getRankName());
     if(rankSong==null){
         log.error("核心歌曲信息缺失");
         return "素材不够，分析师还在查阅资料";
     }
     try{
         RestTemplate restTemplate=new RestTemplate();
         HttpHeaders headers=new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization","Bearer"+apiKey);
         String userContent=String.format("你是一个资格榜单分析师。用户正在听《%s》歌，歌手是%s,标签是%s。请结合这些信息，"+"" +
                 "请用温柔的语气给这个榜单做一个犀利，专业的分析评价，并能够进行总结，总结出榜单风格等等。字数20-50字。",
                 rankSong.getSongName(),
                 rankSong.getSingerName(),
                 rankSong.getRanking(),
                 rankTags.getRTId());
         Map<String,Object> body=new HashMap<>();
         body.put("model",MODEL);
         body.put("temperature",0.8);
         body.put("message", Collections.singletonList(Map.of("role","user","content",userContent)));
         HttpEntity<Map<String,Object>> request=new HttpEntity<>(body,headers);
         ResponseEntity<Map> response=restTemplate.postForEntity(API_URL,request, Map.class);
         // 安全解析
         Map<String, Object> resBody = response.getBody();
         if (resBody == null || !resBody.containsKey("choices")) {
             return "AI 暂时无法分析";
         }

         // 解析 JSON ( choices[0].message.content )
         List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
         Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
         return (String) message.get("content");

     } catch (Exception e) {
         log.error("AI调用异常: ", e);
         return "抱歉，分析师去喝咖啡了，请稍后再试。";
     }
     }
    }

