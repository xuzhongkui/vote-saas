package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.ChatMessage;
import com.jm.vote.entity.User;
import com.jm.vote.service.ChatService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtil securityUtil;

    /**
     * 发送消息（用户）
     * 换绑后：只能向当前绑定的商家发送消息，历史客服不可再联系
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> sendMessage(
            HttpServletRequest request,
            @RequestParam Long merchantId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") String messageType) {
        
        Long userId = securityUtil.getCurrentUserId();
        Long currentMerchantId = securityUtil.getCurrentMerchantId();
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 验证用户只能向当前绑定的商家发送消息
        if (currentMerchantId == null || !currentMerchantId.equals(merchantId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "只能向当前绑定的商家发送消息"));
        }
        
        ChatMessage message = chatService.sendMessage(merchantId, userId, "USER", userId, content, messageType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取聊天记录
     * 换绑后：只显示当前merchantId的聊天记录，历史客服不可再联系
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT')")
    public ResponseEntity<Page<ChatMessage>> getChatHistory(
            HttpServletRequest request,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long currentUserId = securityUtil.getCurrentUserId();
        Long currentMerchantId = securityUtil.getCurrentMerchantId();
        
        // 用户只能查看自己的聊天记录，且只能查看当前绑定商家的聊天记录
        if (currentUserId != null) {
            userId = currentUserId;
            // 用户只能查看当前绑定商家的聊天记录
            if (merchantId != null && currentMerchantId != null && !currentMerchantId.equals(merchantId)) {
                return ResponseEntity.badRequest().build();
            }
            merchantId = currentMerchantId;
        } else if (currentMerchantId != null) {
            // 商家可以查看所有绑定用户的聊天记录，merchantId 从后端获取
            merchantId = currentMerchantId;
        }
        
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<ChatMessage> messages = chatService.getChatHistory(merchantId, userId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT')")
    public ResponseEntity<Map<String, Object>> markAsRead(
            HttpServletRequest request,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long userId,
            @RequestParam String senderType) {
        
        Long currentUserId = securityUtil.getCurrentUserId();
        Long currentMerchantId = securityUtil.getCurrentMerchantId();
        
        if (currentUserId != null) {
            userId = currentUserId;
            merchantId = currentMerchantId;
        } else if (currentMerchantId != null) {
            merchantId = currentMerchantId;
        }
        
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        chatService.markAsRead(merchantId, userId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT')")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            HttpServletRequest request,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long userId,
            @RequestParam String senderType) {
        
        Long currentUserId = securityUtil.getCurrentUserId();
        Long currentMerchantId = securityUtil.getCurrentMerchantId();
        
        if (currentUserId != null) {
            userId = currentUserId;
            merchantId = currentMerchantId;
        } else if (currentMerchantId != null) {
            merchantId = currentMerchantId;
        }
        
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Long count = chatService.getUnreadCount(merchantId, userId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 发送消息（商家）
     */
    @PostMapping("/merchant/send")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> sendMerchantMessage(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") String messageType) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 验证用户是否绑定当前商家
        User user = chatService.getUserById(userId);
        if (user == null || !merchantId.equals(user.getMerchantId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "该用户未绑定您的商家"));
        }
        
        ChatMessage message = chatService.sendMessage(merchantId, userId, "MERCHANT", merchantId, content, messageType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取聊天用户列表（商家）
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<User>> getChatUsers(HttpServletRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<User> users = chatService.getChatUsers(merchantId);
        return ResponseEntity.ok(users);
    }

    /**
     * 获取商家与用户的聊天记录（商家端专用）
     */
    @GetMapping("/merchant/history")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<ChatMessage>> getMerchantUserChatHistory(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 查询商家与用户的聊天记录（自动包含 merchant_id 过滤）
        Page<ChatMessage> messages = chatService.getChatHistory(merchantId, userId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取商家与用户的未读消息数（商家端专用）
     */
    @GetMapping("/merchant/unread")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> getMerchantUserUnreadCount(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam String senderType) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 统计商家与用户的未读消息数（自动包含 merchant_id 过滤）
        Long count = chatService.getUnreadCount(merchantId, userId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 标记商家与用户的消息为已读（商家端专用）
     */
    @PostMapping("/merchant/read")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> markMerchantUserAsRead(
            HttpServletRequest request,
            @RequestParam Long userId,
            @RequestParam String senderType) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 标记商家与用户的消息为已读（自动包含 merchant_id 过滤）
        chatService.markAsRead(merchantId, userId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 发送消息给管理员（商家）
     */
    @PostMapping("/merchant/admin/send")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> sendMerchantToAdminMessage(
            HttpServletRequest request,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") String messageType) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        ChatMessage message = chatService.sendMessage(merchantId, null, "MERCHANT", merchantId, content, messageType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商家与管理员的聊天记录
     */
    @GetMapping("/merchant/admin/history")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<ChatMessage>> getMerchantAdminChatHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<ChatMessage> messages = chatService.getAdminChatHistory(merchantId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取商家未读管理员消息数
     */
    @GetMapping("/merchant/admin/unread")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> getMerchantAdminUnreadCount(HttpServletRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Long count = chatService.getAdminUnreadCount(merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 标记商家消息为已读（管理员）
     */
    @PostMapping("/merchant/admin/read")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> markMerchantAdminAsRead(
            HttpServletRequest request,
            @RequestParam String senderType) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        chatService.markAdminAsRead(merchantId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // ==================== 管理员与商家聊天 ====================

    /**
     * 发送消息（管理员）
     */
    @PostMapping("/admin/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> sendAdminMessage(
            HttpServletRequest request,
            @RequestParam Long merchantId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") String messageType) {
        
        Long adminId = securityUtil.getCurrentAdminId();
        if (adminId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        ChatMessage message = chatService.sendAdminMessage(merchantId, adminId, content, messageType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取管理员与商家的聊天记录
     */
    @GetMapping("/admin/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ChatMessage>> getAdminChatHistory(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ChatMessage> messages = chatService.getAdminChatHistory(merchantId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 标记管理员消息为已读
     */
    @PostMapping("/admin/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> markAdminAsRead(
            @RequestParam Long merchantId,
            @RequestParam String senderType) {
        
        chatService.markAdminAsRead(merchantId, senderType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取管理员未读消息数
     */
    @GetMapping("/admin/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminUnreadCount(
            @RequestParam Long merchantId) {
        
        Long count = chatService.getAdminUnreadCount(merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}

