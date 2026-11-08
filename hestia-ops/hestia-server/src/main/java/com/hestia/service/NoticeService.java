package com.hestia.service;

import com.hestia.entity.Notice;

import java.util.List;

/**
 * 站内通知服务
 */
public interface NoticeService {

    /** 落库并通过 WebSocket 推送 */
    void push(Long receiverId, Integer receiverType, Integer bizType, Long bizId, String title, String content);

    List<Notice> list(Long receiverId, Integer receiverType);

    Long countUnread(Long receiverId, Integer receiverType);

    void markRead(Long id);

    void markAllRead(Long receiverId, Integer receiverType);
}
