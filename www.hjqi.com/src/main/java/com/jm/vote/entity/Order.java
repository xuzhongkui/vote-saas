package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
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

    private LocalDateTime confirmedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}


