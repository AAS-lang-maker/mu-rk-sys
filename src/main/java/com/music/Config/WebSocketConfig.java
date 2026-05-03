package com.music.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    /**
     * 这个 Bean 是必须的！
     * 它会自动注册使用了 @ServerEndpoint 注解的 Bean。
     * 如果没有这个，你的 NativeWebSocketServer 将不会被扫描到。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}