package com.music.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
/*
@Configuration
// 1. 开启 WebSocket 消息代理功能
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 2. 配置消息代理（定义消息的前缀）
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 开启一个简单的内存消息代理
        // "/queue" 前缀用于点对点（发给特定用户）
        // "/topic" 前缀用于广播（全服通知）
        registry.enableSimpleBroker("/queue", "/topic");

        // 定义应用的前缀，比如客户端发给服务端的消息要以 "/app" 开头
        registry.setApplicationDestinationPrefixes("/app");

        // 【关键】定义用户专属消息的前缀
        // 当使用 convertAndSendToUser 时，Spring 会自动把消息发到 "/user/{userId}/..."
        registry.setUserDestinationPrefix("/user");
    }

    // 3. 注册 STOMP 端点（客户端连接的地方）
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 定义握手端点，允许跨域
        registry.addEndpoint("/ws-endpoint")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 开启 SockJS 支持（防止浏览器不支持 WebSocket）
    }
}*/

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 开启一个简单的内存消息代理
        registry.enableSimpleBroker("/topic", "/queue");

        // 【关键】定义点对点消息的前缀，默认就是 /user
        // 后端 sendToUser 时，会自动把消息发到 /user/{userId}/{destination}
        registry.setUserDestinationPrefix("/user");

        // 定义应用端点前缀
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 协议的端点，前端通过这个连接
        registry.addEndpoint("/ws-endpoint").withSockJS();
    }
}