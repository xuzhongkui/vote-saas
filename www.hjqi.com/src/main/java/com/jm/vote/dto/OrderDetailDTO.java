package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {

    private Long id;

    private String orderNo;

    private Long userId;
    
    private Long merchantId;

    private String status;

    private BigDecimal productAmount;

    private BigDecimal deliveryFee;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private Integer productCount;

    private Integer deliveryType;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDate expectedDate;

    private String expectedTime;

    private String userRemark;

    private String merchantRemark;

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    private List<OrderItemDTO> items;
    
    /**
     * 是否只读（换绑前的订单为只读）
     */
    private Boolean readOnly;
}


