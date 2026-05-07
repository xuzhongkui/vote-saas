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
@TableName("t_merchant_ticket")
public class MerchantTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private Long merchantId;

    private Long userId;

    private String category;

    private String priority;

    private String status;

    private String title;

    private String content;

    private Long adminId;

    private String adminReply;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

