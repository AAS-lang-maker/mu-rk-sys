package com.music.Controller;

import com.music.Service.AiCommentService;
import com.music.dto.MyRankWithSong;
import com.music.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/AiComment")
public class AiCommentController {
    @Autowired
    private AiCommentService aiCommentService;
    @PostMapping("/ai")
    public Result<String> aiComment(@RequestParam("rankId")Integer rankId,
                                    @RequestBody MyRankWithSong myRankWithSong, @RequestParam("token")String token) {
        if(token==null||token.isEmpty()){
        return Result.error("请先登录");
        }
        if(rankId==null||rankId==0){
            return Result.error("必须要选中榜单哦");
        }
        String comment = aiCommentService.getAiComment(myRankWithSong);
        if (!(comment == null)) {
             return Result.success(comment);
        }else{
            return Result.error("AI锐评生成失败了呢");
        }
    }
}
