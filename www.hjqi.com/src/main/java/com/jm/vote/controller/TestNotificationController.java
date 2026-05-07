package com.jm.vote.controller;

import com.jm.vote.service.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestNotificationController {

    private final NotificationWebSocketService notificationWebSocketService;

    /**
     * 测试发送订单通知给商家
     */
    @PostMapping("/notification/order/{merchantId}")
    public Map<String, Object> testOrderNotification(@PathVariable Long merchantId) {
        log.info("🧪 测试发送订单通知给商家: merchantId={}", merchantId);
        
        try {
            notificationWebSocketService.sendOrderStatusNotification(
                merchantId, 
                999L, 
                "PAID", 
                "TEST-ORDER-" + System.currentTimeMillis()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "订单通知已发送");
            result.put("merchantId", merchantId);
            return result;
        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 测试发送聊天通知给商家
     */
    @PostMapping("/notification/chat/{merchantId}")
    public Map<String, Object> testChatNotification(@PathVariable Long merchantId) {
        log.info("🧪 测试发送聊天通知给商家: merchantId={}", merchantId);
        
        try {
            notificationWebSocketService.sendChatNotification(
                merchantId, 
                1L, 
                "USER", 
                "这是一条测试消息"
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "聊天通知已发送");
            result.put("merchantId", merchantId);
            return result;
        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 测试发送系统通知给商家
     */
    @PostMapping("/notification/system/{merchantId}")
    public Map<String, Object> testSystemNotification(@PathVariable Long merchantId) {
        log.info("🧪 测试发送系统通知给商家: merchantId={}", merchantId);
        
        try {
            notificationWebSocketService.sendSystemNotification(
                merchantId, 
                "系统通知测试", 
                "这是一条测试系统通知"
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "系统通知已发送");
            result.put("merchantId", merchantId);
            return result;
        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
            return result;
        }
    }
}

