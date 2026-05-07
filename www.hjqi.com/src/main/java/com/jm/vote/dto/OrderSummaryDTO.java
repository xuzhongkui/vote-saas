package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderSummaryDTO {

    private Long id;

    private String orderNo;

    private String status;

    private BigDecimal totalAmount;

    private Integer productCount;

    private LocalDateTime createdAt;
    
    /**
     * 是否只读（换绑前的订单为只读）
     */
    private Boolean readOnly;
    
    /**
     * 订单商品列表
     */
    private List<OrderItemDTO> items;
}


