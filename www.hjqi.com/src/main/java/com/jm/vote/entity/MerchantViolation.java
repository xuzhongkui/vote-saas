package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_merchant_violation")
public class MerchantViolation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String violationType;

    private String title;

    private String description;

    private String penaltyType;

    private BigDecimal penaltyAmount;

    private Integer penaltyDurationDays;

    private String status;

    private Long adminId;

    private String adminRemark;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

