package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MerchantStatisticsDTO {
    // 今日统计
    private Long todayOrderCount;
    private BigDecimal todaySalesAmount;
    
    // 本周统计
    private Long weekOrderCount;
    private BigDecimal weekSalesAmount;
    
    // 本月统计
    private Long monthOrderCount;
    private BigDecimal monthSalesAmount;
    
    // 总统计
    private Long totalOrderCount;
    private BigDecimal totalSalesAmount;
    
    // 用户数
    private Long userCount;
    
    // 商品数
    private Long productCount;
    
    // 订单状态统计
    private Long pendingOrderCount;
    private Long confirmedOrderCount;
    private Long shippingOrderCount;
    private Long completedOrderCount;
    private Long cancelledOrderCount;
}

