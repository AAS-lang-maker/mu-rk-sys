package com.music.Service.impl;

import com.music.Service.AiCommentService;
import com.music.dto.MyRankWithSong;
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
    public String getAiComment(MyRankWithSong myRankWithSong) {
     log.info("AI锐评启动，正在分析用户喜欢的榜单：{}",myRankWithSong);
     if(myRankWithSong.getRankId()==null){
         log.error("核心歌曲信息缺失");
         return "素材不够，分析师还在查阅资料";
     }

        // 详细参数校验（增加日志）
        if (myRankWithSong.getRankName() == null || myRankWithSong.getRankName().isEmpty()) {
            log.warn("榜单名称不能为空");

        }
        if (myRankWithSong.getRankSongList() == null || myRankWithSong.getRankSongList().isEmpty()) {
            log.warn("榜单内的歌曲内容为空");
        }

    //    if (myRankWithSong.getRankTagsList() == null || myRankWithSong.getRankTagsList() == 0) {
         //   log.warn("标签信息缺失, rankTags={}, rTId={}", rankTags, rankTags != null ? rankTags.getRTId() : "null");
      //  }
        StringBuilder stringBuilder=new StringBuilder();
        List<RankSong> rankSongList=myRankWithSong.getRankSongList();
        for(int  i=0;i<rankSongList.size();i++){
            RankSong s=rankSongList.get(i);
            stringBuilder.append(String.format("%d.《%s》-%s\n",s.getRanking(),s.getSongName(),s.getSingerName()));
        }
//        List<RankTags> rankTags=myRankWithSong.getRankTagsList();
//        List<String> tagName=new ArrayList<>();
//        for(RankTags r:rankTags){
//            if(r.getTagName()!=null){
//                tagNames.add(r.getTagName());
//            }
//        }
//        String finalTags=String.join(",",tagName);
     try{
         RestTemplate restTemplate=new RestTemplate();
         HttpHeaders headers=new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("Authorization","Bearer "+apiKey);
         String userContent=String.format("你是一个资格榜单分析师。用户当前是想分析《%s》这个榜单啊，里面的具体歌曲内容如下：" +
                         "\n%s\n"+"请结合这些歌曲的名称，排名顺序歌手，" +
                         "请用温柔的语气给这个榜单做一个犀利，专业的分析评价，并能够进行总结，总结出榜单风格等等。字数20-50字。",
                 myRankWithSong.getRankName(),
                 stringBuilder.toString()
               //  myRankWithSong.getRankTagsList()
                 );
                 log.debug("发送给AI的prompt: {}", userContent);
         Map<String,Object> body=new HashMap<>();
         body.put("model",MODEL);
         body.put("temperature",0.8);
         body.put("messages", Collections.singletonList(Map.of("role","user","content",userContent)));
         HttpEntity<Map<String,Object>> request=new HttpEntity<>(body,headers);
         ResponseEntity<Map> response=restTemplate.postForEntity(API_URL,request, Map.class);
         // 安全解析
         Map<String, Object> resBody = response.getBody();
         if (resBody == null) {
             log.error("API返回空响应体");
             return "AI 暂时无法分析";
         }

         if (!resBody.containsKey("choices")) {
             log.error("API响应缺少choices字段, 响应内容: {}", resBody);
             return "AI 暂时无法分析";
         }

         // 解析 JSON ( choices[0].message.content )
         List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
         if (choices == null || choices.isEmpty()) {
             log.error("API返回choices为空");
             return "AI 暂时无法分析";
         }
         Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
         if (message == null) {
             log.error("API返回message为空");
             return "AI 暂时无法分析";
         }

         String content = (String) message.get("content");
         log.info("AI生成成功，内容长度: {}", content != null ? content.length() : 0);
         return (String) message.get("content");

     } catch (Exception e) {
         log.error("AI调用异常: ", e);
         return "抱歉，分析师去喝咖啡了，请稍后再试。";
     }
     }
    }

