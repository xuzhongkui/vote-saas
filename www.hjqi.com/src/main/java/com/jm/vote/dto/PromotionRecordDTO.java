package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推广记录DTO
 */
@Data
public class PromotionRecordDTO {
    
    private Long id;
    
    private Long merchantId;
    
    private Long inviterId;
    
    private Long inviteeId;
    
    private String inviteeType; // MERCHANT/USER
    
    private String inviteeName; // 被邀请人名称（商家店铺名称或用户用户名）
    
    private String promotionCode;
    
    private Integer rewardAmount; // 奖励金额（分）
    
    private String rewardType;
    
    private BigDecimal rewardRate;
    
    private BigDecimal orderAmount;
    
    private Integer status; // 0-待确认 1-已确认 2-已取消
    
    private LocalDateTime createdAt;
    
    private LocalDateTime confirmedAt;
    
    private LocalDateTime settlementTime;
    
    private String remark;
}

