package com.hestia.service.impl;

import com.alibaba.fastjson.JSON;
import com.hestia.entity.Notice;
import com.hestia.mapper.NoticeMapper;
import com.hestia.service.NoticeService;
import com.hestia.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内通知实现：先落库保证不丢，再尝试 WebSocket 实时推送。
 * 推送失败只记录日志，不影响主流程。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final WebSocketServer webSocketServer;

    @Override
    public void push(Long receiverId, Integer receiverType, Integer bizType, Long bizId, String title, String content) {
        Notice notice = new Notice();
        notice.setReceiverId(receiverId);
        notice.setReceiverType(receiverType);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setBizType(bizType);
        notice.setBizId(bizId);
        notice.setIsRead(0);
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NOTICE");
        payload.put("noticeId", notice.getId());
        payload.put("bizType", bizType);
        payload.put("bizId", bizId);
        payload.put("title", title);
        payload.put("content", content);
        try {
            webSocketServer.sendToUser(receiverType + "-" + receiverId, JSON.toJSONString(payload));
        } catch (Exception e) {
            log.warn("WebSocket 推送失败，通知已落库 receiver={}-{}", receiverType, receiverId);
        }
    }

    @Override
    public List<Notice> list(Long receiverId, Integer receiverType) {
        return noticeMapper.selectByReceiver(receiverId, receiverType);
    }

    @Override
    public Long countUnread(Long receiverId, Integer receiverType) {
        return noticeMapper.countUnread(receiverId, receiverType);
    }

    @Override
    public void markRead(Long id) {
        noticeMapper.markRead(id);
    }

    @Override
    public void markAllRead(Long receiverId, Integer receiverType) {
        noticeMapper.markAllRead(receiverId, receiverType);
    }
}
