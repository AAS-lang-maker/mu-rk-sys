package com.music.Controller;

import com.music.Service.AiCommentService;
import com.music.dto.AiCommentVo;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankSong;
import com.music.pojo.RankTags;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static net.sf.jsqlparser.parser.feature.Feature.comment;

@RestController
@RequestMapping("/AiComment")
public class AiCommentController {
    @Autowired
    private AiCommentService aiCommentService;
    @PostMapping("/ai")
    public Result<String> aiComment(@RequestBody AiCommentVo aiCommentVo, @RequestParam("token")String token) {
        if(token.isEmpty()||token==null){
        return Result.error("请先登录");
        }
        String comment = aiCommentService.getAiComment(aiCommentVo.getRankSong(),aiCommentVo.getPersonalRank(),
                aiCommentVo.getRankTags());
        if (!(comment == null)) {
            return Result.success("AI锐评即将呈现");
        }else{
            return Result.error("AI锐评生成失败了呢");
        }
    }
}
