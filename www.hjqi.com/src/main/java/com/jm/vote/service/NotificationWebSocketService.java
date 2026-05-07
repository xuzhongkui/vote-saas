package com.jm.vote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送订单状态推送通知给商家
     */
    public void sendOrderStatusNotification(Long merchantId, Long orderId, String status, String orderNo) {
        log.info("📤 准备推送订单通知: merchantId={}, orderId={}, status={}, orderNo={}", 
            merchantId, orderId, status, orderNo);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_STATUS");
        notification.put("orderId", orderId);
        notification.put("orderNo", orderNo);
        notification.put("status", status);
        notification.put("timestamp", System.currentTimeMillis());

        String topic = "/topic/merchant/" + merchantId;
        log.info("📡 推送到主题: {}", topic);
        log.info("📦 推送内容: {}", notification);
        
        try {
            messagingTemplate.convertAndSend(topic, notification);
            log.info("✅ 订单通知推送成功: merchantId={}, orderNo={}, status={}", merchantId, orderNo, status);
        } catch (Exception e) {
            log.error("❌ 订单通知推送失败: merchantId={}, orderNo={}, error={}", 
                merchantId, orderNo, e.getMessage(), e);
        }
    }

    /**
     * 发送订单状态推送通知给用户
     */
    public void sendOrderStatusNotificationToUser(Long userId, Long orderId, String status, String orderNo) {
        log.info("📤 准备推送订单通知给用户: userId={}, orderId={}, status={}, orderNo={}", 
            userId, orderId, status, orderNo);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_STATUS");
        notification.put("orderId", orderId);
        notification.put("orderNo", orderNo);
        notification.put("status", status);
        notification.put("timestamp", System.currentTimeMillis());

        String topic = "/topic/user/" + userId;
        log.info("📡 推送到主题: {}", topic);
        
        try {
            messagingTemplate.convertAndSend(topic, notification);
            log.info("✅ 订单通知推送给用户成功: userId={}, orderNo={}, status={}", userId, orderNo, status);
        } catch (Exception e) {
            log.error("❌ 订单通知推送给用户失败: userId={}, orderNo={}, error={}", 
                userId, orderNo, e.getMessage(), e);
        }
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(Long merchantId, String title, String content) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM");
        notification.put("title", title);
        notification.put("content", content);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/merchant/" + merchantId, notification);
        log.info("系统通知已推送: merchantId={}, title={}", merchantId, title);
    }

    /**
     * 发送客服消息通知
     */
    public void sendChatNotification(Long merchantId, Long userId, String senderType, String content) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHAT");
        notification.put("userId", userId);
        notification.put("senderType", senderType);
        notification.put("content", content);
        notification.put("timestamp", System.currentTimeMillis());

        if ("USER".equals(senderType)) {
            messagingTemplate.convertAndSend("/topic/merchant/" + merchantId + "/chat", notification);
        } else {
            messagingTemplate.convertAndSend("/topic/user/" + userId + "/chat", notification);
        }
    }
}

