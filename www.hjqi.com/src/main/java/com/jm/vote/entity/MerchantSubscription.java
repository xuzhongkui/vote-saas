package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_merchant_subscription")
public class MerchantSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long planId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private Integer autoRenew;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

