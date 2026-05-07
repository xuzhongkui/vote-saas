package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.ChatMessage;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.User;
import com.jm.vote.repository.ChatMessageMapper;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.UserMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送消息
     */
    @Transactional
    public ChatMessage sendMessage(Long merchantId, Long userId, String senderType, Long senderId, String content, String messageType) {
        ChatMessage message = ChatMessage.builder()
                .merchantId(merchantId)
                .userId(userId)
                .senderType(senderType)
                .senderId(senderId)
                .content(content)
                .messageType(messageType != null ? messageType : "TEXT")
                .isRead(0)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatMessageMapper.insert(message);
        
        // 通过WebSocket推送消息
        try {
            java.util.Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("type", "CHAT");
            notification.put("merchantId", merchantId);
            notification.put("userId", userId);
            notification.put("senderType", senderType);
            notification.put("senderId", senderId);
            notification.put("content", content);
            notification.put("timestamp", System.currentTimeMillis());
            
            // 添加发送者名称
            if ("USER".equals(senderType) && userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    notification.put("senderName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            } else if ("MERCHANT".equals(senderType) && merchantId != null) {
                Merchant merchant = merchantMapper.selectById(merchantId);
                if (merchant != null) {
                    notification.put("senderName", merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getShopNameEn());
                }
            }
            
            if ("MERCHANT".equals(senderType) && userId != null) {
                // 商家发送给用户
                messagingTemplate.convertAndSend("/topic/user/" + userId + "/chat", notification);
                log.info("聊天消息已推送给用户: userId={}", userId);
            } else if ("USER".equals(senderType)) {
                // 用户发送给商家
                messagingTemplate.convertAndSend("/topic/merchant/" + merchantId + "/chat", notification);
                log.info("聊天消息已推送给商家: merchantId={}", merchantId);
            } else if ("MERCHANT".equals(senderType) && userId == null) {
                // 商家发送给管理员（通过管理员聊天接口）
                messagingTemplate.convertAndSend("/topic/admin/chat", notification);
                log.info("聊天消息已推送给管理员");
            }
        } catch (Exception e) {
            log.error("推送聊天消息失败: {}", e.getMessage(), e);
        }
        
        return message;
    }

    /**
     * 获取聊天记录
     * 换绑后：只显示当前merchantId的聊天记录，历史客服不可再联系
     */
    public Page<ChatMessage> getChatHistory(Long merchantId, Long userId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        // 只查询当前merchantId的聊天记录，换绑后历史聊天记录自动隔离
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getDeleted, 0)
                .orderByDesc(ChatMessage::getCreatedAt);
        
        return chatMessageMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long merchantId, Long userId, String senderType) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getSenderType, senderType)
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getDeleted, 0);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        for (ChatMessage message : messages) {
            message.setIsRead(1);
            message.setReadAt(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
    }

    /**
     * 获取未读消息数
     */
    public Long getUnreadCount(Long merchantId, Long userId, String senderType) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getSenderType, senderType)
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getDeleted, 0);
        
        return chatMessageMapper.selectCount(wrapper);
    }

    /**
     * 获取用户列表（有聊天记录的用户）
     * 换绑后：只显示当前绑定该商家的用户
     */
    public List<User> getChatUsers(Long merchantId) {
        // 只返回当前绑定该商家的用户（换绑后的用户会自动显示，历史用户自动隐藏）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getMerchantId, merchantId)
                .eq(User::getDeleted, 0)
                .eq(User::getStatus, 1);
        
        return userMapper.selectList(wrapper);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    // ==================== 管理员与商家聊天 ====================

    /**
     * 发送消息（管理员）
     */
    @Transactional
    public ChatMessage sendAdminMessage(Long merchantId, Long adminId, String content, String messageType) {
        ChatMessage message = ChatMessage.builder()
                .merchantId(merchantId)
                .userId(null) // 管理员与商家聊天，userId为null
                .senderType("ADMIN")
                .senderId(adminId)
                .content(content)
                .messageType(messageType != null ? messageType : "TEXT")
                .isRead(0)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatMessageMapper.insert(message);
        
        // 通过WebSocket推送消息给商家
        try {
            java.util.Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("type", "CHAT");
            notification.put("merchantId", merchantId);
            notification.put("senderType", "ADMIN");
            notification.put("senderId", adminId);
            notification.put("content", content);
            notification.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/merchant/" + merchantId + "/admin/chat", notification);
            log.info("管理员消息已推送: merchantId={}, adminId={}", merchantId, adminId);
        } catch (Exception e) {
            log.error("推送管理员消息失败: {}", e.getMessage(), e);
        }
        
        return message;
    }

    /**
     * 获取管理员与商家的聊天记录
     */
    public Page<ChatMessage> getAdminChatHistory(Long merchantId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .isNull(ChatMessage::getUserId) // 管理员与商家聊天，userId为null
                .eq(ChatMessage::getDeleted, 0)
                .orderByDesc(ChatMessage::getCreatedAt);
        
        return chatMessageMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 标记管理员消息为已读
     */
    @Transactional
    public void markAdminAsRead(Long merchantId, String senderType) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .isNull(ChatMessage::getUserId)
                .eq(ChatMessage::getSenderType, senderType)
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getDeleted, 0);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        for (ChatMessage message : messages) {
            message.setIsRead(1);
            message.setReadAt(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
    }

    /**
     * 获取管理员未读消息数
     */
    public Long getAdminUnreadCount(Long merchantId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getMerchantId, merchantId)
                .isNull(ChatMessage::getUserId)
                .eq(ChatMessage::getSenderType, "MERCHANT")
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getDeleted, 0);
        
        return chatMessageMapper.selectCount(wrapper);
    }
}

