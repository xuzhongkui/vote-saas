package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long userId;

    private String senderType;

    private Long senderId;

    private String content;

    private String messageType;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private Integer deleted;
}

