package com.music.Controller;

import com.music.Service.SongService;
import com.music.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/api/songGo")
@Tag(name = "歌曲播放相关接口", description = "提供歌曲播放的接口")
public class SongController {
    @Autowired
    private SongService songService;
    @GetMapping("/play/{songId}")
    @Operation(summary = "播放歌曲", description = "根据歌曲ID播放歌曲")
    public void playSong(@RequestParam("token")String token,@PathVariable("songId")Integer songId,
                                   HttpServletResponse response) throws IOException {
        if(token==null||token.equals("")){
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("token不能为空");
            return;//这里一定要有，用来结束对token的判断
        }
        try{
            songService.playSong(songId, (jakarta.servlet.http.HttpServletResponse) response);
        }catch (RuntimeException e){
            response.setStatus(HttpStatus.BAD_REQUEST.value());//设置Http的状态响应码
            try{
                //下面语句的判断依旧权威，因为上方RuntimeException在返回null时，下方的getMessage也会接受到null
                //从而触发空指针异常
                String errorMsg = e.getMessage()!=null?e.getMessage():"再不測試成功我要暴斃了";
                response.getWriter().write(errorMsg);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
