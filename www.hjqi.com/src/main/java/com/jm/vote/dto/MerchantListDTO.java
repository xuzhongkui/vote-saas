package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantListDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String shopNameZh;
    private String shopNameEn;
    private String logo;
    private String contactName;
    private String contactPhone;
    private String inviteCode;
    private String promotionCode;
    private String referrerCode;
    private Integer status;
    private Integer paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private String auditRemark;
    private LocalDateTime auditTime;
    private Long auditorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

