package com.jm.vote.dto;

import lombok.Data;

@Data
public class AdminStatisticsDTO {
    // 商家统计
    private Long totalMerchantCount;
    private Long pendingMerchantCount;
    private Long activeMerchantCount;
    private Long disabledMerchantCount;
    
    // 用户统计
    private Long totalUserCount;
    
    // 订单统计
    private Long totalOrderCount;
    private Long todayOrderCount;
    private Long weekOrderCount;
    private Long monthOrderCount;
}

