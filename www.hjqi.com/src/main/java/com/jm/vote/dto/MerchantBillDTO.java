package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MerchantBillDTO {
    private Long id;
    private Long merchantId;
    private String merchantName; // 商家名称
    private String billNo;
    private String billType;
    private BigDecimal amount;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String invoiceUrl;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

