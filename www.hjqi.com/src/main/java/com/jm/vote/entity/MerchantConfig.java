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
@TableName("t_merchant_config")
public class MerchantConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String pickupAddressZh;

    private String pickupAddressEn;

    private String deliveryAreaZh;

    private String deliveryAreaEn;

    private BigDecimal minOrderAmount;

    private BigDecimal deliveryFee;

    private BigDecimal freeDeliveryAmount;

    private Boolean supportPickup;

    private Boolean supportDelivery;

    /**
     * JSON 字符串保存
     */
    private String deliveryTimeSlots;

    private String pickupTimeSlots;

    private Integer advanceBookingDays;

    private BigDecimal maxDeliveryDistance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

