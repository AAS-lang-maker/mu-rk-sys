package com.music.Controller;

import com.music.Service.MusicAgent;
import com.music.Service.MusicAgentService;
import com.music.utils.Result;
import com.music.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Controller
@RequestMapping("api/agent")
@CrossOrigin
@Slf4j
public class MusicAgentController {
    @Autowired
    private MusicAgentService musicAgentService;
    @Autowired
    private MusicAgent musicAgent;
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter  chat(@RequestParam("token")String token,
                           @RequestParam("userMessage")String userMessage){
        int userId= UserContext.getUserId();
        log.info("收到用户聊天请求，准备调用AI,用户的userId:{}",userId);
        SseEmitter emitter = new SseEmitter(60000L);

// 2. 注意,这里的参数名要对上上面的 userMessage，别写成 message
        musicAgentService.chat(userId, userMessage)
                .onNext(aiContent -> { // 3. 核心改动,这里改名叫 aiContent，别叫 token！
                    try {
                        emitter.send(aiContent);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onComplete(response -> {
                    emitter.complete();
                })
                .onError(emitter::completeWithError)
                .start();
        log.info("调用Agent结束，返回聊天文字");
        return emitter;
    }
}
