package com.jm.vote.dto;

import lombok.Data;

@Data
public class PromotionStatisticsDTO {
    // 推广商家数
    private Long promotedMerchantCount;
    
    // 推广用户数
    private Long promotedUserCount;
    
    // 总奖励金额（分）
    private Long totalRewardAmount;
    
    // 已确认奖励金额（分）
    private Long confirmedRewardAmount;
    
    // 待确认奖励金额（分）
    private Long pendingRewardAmount;
}

