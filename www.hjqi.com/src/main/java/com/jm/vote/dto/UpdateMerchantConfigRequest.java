package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UpdateMerchantConfigRequest {
    private String pickupAddressZh;
    private String pickupAddressEn;
    private String deliveryAreaZh;
    private String deliveryAreaEn;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private BigDecimal freeDeliveryAmount;
    private Boolean supportPickup;
    private Boolean supportDelivery;
    private Map<String, Object> deliveryTimeSlots;
    private Map<String, Object> pickupTimeSlots;
    private Integer advanceBookingDays;
    private BigDecimal maxDeliveryDistance;
}

