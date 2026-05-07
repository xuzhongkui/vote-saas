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
@TableName("t_merchant_notification_config")
public class MerchantNotificationConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String notificationType;

    private String notificationChannel;

    private Integer enabled;

    private String configJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

