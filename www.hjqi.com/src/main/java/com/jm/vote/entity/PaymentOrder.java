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
@TableName("t_payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long merchantId;

    private String orderType;

    private String description;

    private String paymentNo;

    private String paymentType;

    private BigDecimal amount;

    private String status;

    private String thirdPartyOrderNo;

    private String callbackData;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

