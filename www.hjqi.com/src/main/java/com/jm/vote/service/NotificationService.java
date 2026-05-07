package com.jm.vote.service;

import com.jm.vote.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationWebSocketService notificationWebSocketService;

    /**
     * 发送订单通知（异步执行，不阻塞主流程）
     * 只通过WebSocket推送到通知中心，不发送邮件
     */
    @Async
    public void sendOrderNotification(Long merchantId, Long orderId, String type, Order order) {
        String title = "";
        String content = "";
        
        switch (type) {
            case "ORDER_NEW":
                title = "新订单通知";
                content = String.format("您有一个新订单！订单号：%s，金额：%.2f元",
                        order.getOrderNo(), order.getTotalAmount());
                break;
            case "ORDER_CONFIRMED":
                title = "订单已确认";
                content = String.format("订单 %s 已确认。", order.getOrderNo());
                break;
            case "ORDER_SHIPPED":
                title = "订单已发货";
                content = String.format("订单 %s 已发货。", order.getOrderNo());
                break;
            case "ORDER_COMPLETED":
                title = "订单已完成";
                content = String.format("订单 %s 已完成。", order.getOrderNo());
                break;
            case "ORDER_CANCELLED":
                title = "订单已取消";
                content = String.format("订单 %s 已取消。", order.getOrderNo());
                break;
        }
        
        // 1. 发送 WebSocket 实时推送给商家
        try {
            notificationWebSocketService.sendOrderStatusNotification(merchantId, orderId, order.getStatus(), order.getOrderNo());
            log.info("订单WebSocket通知已推送给商家: merchantId={}, orderNo={}", merchantId, order.getOrderNo());
        } catch (Exception e) {
            log.error("发送订单WebSocket通知失败", e);
        }
        
        // 2. 发送 WebSocket 实时推送给用户
        if (order.getUserId() != null) {
            try {
                notificationWebSocketService.sendOrderStatusNotificationToUser(order.getUserId(), orderId, order.getStatus(), order.getOrderNo());
                log.info("订单WebSocket通知已推送给用户: userId={}, orderNo={}", order.getUserId(), order.getOrderNo());
            } catch (Exception e) {
                log.error("发送订单WebSocket通知给用户失败", e);
            }
        }
    }
    
    /**
     * 发送客服消息通知给商家（已由WebSocket服务处理）
     * 此方法保留用于兼容性，实际推送由ChatService直接调用WebSocket服务
     */
    @Async
    public void sendChatNotificationToMerchant(Long merchantId, String senderName, String content) {
        try {
            // 客服消息通知已由ChatService通过WebSocket直接推送
            log.info("客服消息通知: merchantId={}, senderName={}", merchantId, senderName);
        } catch (Exception e) {
            log.error("发送客服消息通知失败", e);
        }
    }
}

