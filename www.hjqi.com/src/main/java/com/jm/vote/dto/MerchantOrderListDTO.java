package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MerchantOrderListDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    /**
     * 用户名称
     */
    private String userName;
    private String receiverName;
    private String receiverPhone;
    private String status;
    private BigDecimal totalAmount;
    private Integer productCount;
    private Integer deliveryType;
    private LocalDate expectedDate;
    private String expectedTime;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    /**
     * 订单商品列表
     */
    private List<OrderItemDTO> items;
}

