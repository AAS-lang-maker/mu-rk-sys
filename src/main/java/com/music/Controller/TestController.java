package com.music.Controller;

    import com.music.Config.NativeWebSocketServer;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;


    @RestController
    public class TestController {

        // 假设你的 WebSocket 服务类叫 WebSocketServer，且有一个静态方法用来发消息
        @GetMapping("/sendPrivate")
        public String sendPrivate() {
            NativeWebSocketServer.sendToUser("1001", "这是给你的私信：今晚加班！1001");
            return "消息已发送";
        }
    }
