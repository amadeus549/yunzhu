package com.hestia.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 推送服务。
 *
 * <p>连接地址：{@code ws://host:8080/ws/{receiverType}-{receiverId}}
 * <ul>
 *   <li>员工端：{@code /ws/1-2} 表示员工 2</li>
 *   <li>住户端：{@code /ws/2-1} 表示住户 1</li>
 * </ul>
 * 会话表用 ConcurrentHashMap，避免多线程推送时并发修改异常。
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("WebSocket 连接建立：{}，当前在线 {} 个", sid, SESSION_MAP.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        // 心跳帧，直接回 pong
        if ("ping".equalsIgnoreCase(message)) {
            sendToUser(sid, "pong");
        }
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("WebSocket 连接断开：{}，当前在线 {} 个", sid, SESSION_MAP.size());
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("sid") String sid) {
        log.warn("WebSocket 异常：{} - {}", sid, error.getMessage());
        SESSION_MAP.remove(sid);
    }

    /**
     * 推送给指定会话，会话不存在时静默忽略（消息已落库，用户下次登录可看到）
     */
    public void sendToUser(String sid, String message) {
        Session session = SESSION_MAP.get(sid);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("推送给 {} 失败", sid, e);
        }
    }

    /**
     * 群发（例如系统广播）
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("群发消息失败", e);
            }
        }
    }

    public int onlineCount() {
        return SESSION_MAP.size();
    }
}
