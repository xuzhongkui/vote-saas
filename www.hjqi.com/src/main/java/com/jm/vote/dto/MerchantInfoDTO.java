package com.jm.vote.dto;

import lombok.Data;

@Data
public class MerchantInfoDTO {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String shopNameZh;

    private String shopNameEn;

    private String logo;

    private String banner;

    private String contactName;

    private String contactPhone;

    private String inviteCode;

    private String promotionCode;

    private String promotionLink;

    private Integer paymentStatus;

    private java.math.BigDecimal paymentAmount;

    private java.time.LocalDateTime paymentTime;

    private Integer status;

    private String serviceWechat;

    private String servicePhone;

    private String businessHours;

    private java.math.BigDecimal totalReward;

    private java.math.BigDecimal availableReward;
}


