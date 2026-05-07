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
@TableName("t_notification_record")
public class NotificationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long orderId;

    private String type; // ORDER_NEW/ORDER_CONFIRMED/ORDER_SHIPPED/ORDER_COMPLETED

    private String title;

    private String content;

    private String recipientEmail;

    private Integer status; // 0-待发送 1-已发送 2-发送失败

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}

