package com.jm.vote.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOrderRequest {

    /**
     * 配送方式 1-自提 2-配送
     */
    private Integer deliveryType;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDate expectedDate;

    private String expectedTime;

    private String userRemark;
}


