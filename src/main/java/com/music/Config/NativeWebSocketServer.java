package com.music.Config;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam; // 1. 导入 PathParam
import jakarta.websocket.server.ServerEndpoint;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap; // 2. 使用 ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/ws-endpoint/{userId}") // 这里的 {userId} 对应下面的 @PathParam
public class NativeWebSocketServer {

    // 存放所有连接（用于广播）
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    // 记录连接数
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    // 存放用户ID和Session的对应关系 (用于私信)
    // 3. 初始化它！防止空指针异常
    private static final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();


    /**
     * 【核心修改】连接建立成功时，把 userId 和 session 存进去
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        String normalizedUserId = normalizeUserId(userId);
        if (normalizedUserId == null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "userId不能为空"));
            } catch (IOException ignored) {
            }
            return;
        }
        // 1. 加入广播集合
        sessions.add(session);
        session.getUserProperties().put("userId", normalizedUserId);

        System.out.println("用户 " + normalizedUserId + " 加入连接，当前连接数：" + sessions.size());

        // 2. 【关键】加入私信集合 (Map)
        clients.put(normalizedUserId, session);

        int count = onlineCount.incrementAndGet();
        System.out.println("用户 " + normalizedUserId + " 加入连接，当前连接数：" + count);
    }

    //自动的 不怕
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        String normalizedUserId = normalizeUserId(userId);
        // 1. 从广播集合移除
        sessions.remove(session);

        // 2. 从私信集合移除：仅移除当前 session 对应的那条，避免误删新连接
        if (normalizedUserId != null) {
            clients.computeIfPresent(normalizedUserId, (k, v) -> v.equals(session) ? null : v);
        } else {
            clients.entrySet().removeIf(entry -> entry.getValue().equals(session));
        }

        int count = onlineCount.decrementAndGet();
        System.out.println("有一连接关闭    ，当前连接数：" + count);
        System.out.println("用户 " + normalizedUserId + " 断开连接，当前连接数：" + clients.size()); // 4. 使用 clients.size() 获取当前连接数

    }

    @OnMessage
    public void onMessage(String message,
                          Session session,
                          @PathParam("userId") String userId) throws IOException {

        System.out.println("收到用户 " + userId + " 的消息: " + message);

        if (message.startsWith("to:")) {

            String[] parts = message.split(":", 3);

            String targetId = parts[1];
            String content = parts[2];

            sendToUser(targetId, content); // ← 这里只发纯 JSON

        } else {

            session.getBasicRemote().sendText(message);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }


    public static void sendToUser(String userId, String jsonMessage) {
        String normalizedUserId = normalizeUserId(userId);
        if (normalizedUserId == null) {
            System.out.println("服务端：目标用户ID为空，无法发送");
            return;
        }

        Session session = clients.get(normalizedUserId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(jsonMessage);
                System.out.println("服务端：已私信给用户 " + normalizedUserId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("服务端：用户 " + normalizedUserId + " 不在线");
        }
    }


    public static void sendMessageToAll(String message) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String normalizeUserId(String userId) {
        if (userId == null) {
            return null;
        }
        String trimmed = userId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // setClients 方法可以删了，因为我们在 onOpen 里直接 put 了，不再需要外部注入
}