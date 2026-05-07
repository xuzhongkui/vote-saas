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
@TableName("t_merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String shopNameZh;

    private String shopNameEn;

    private String logo;

    private String banner;

    private String descriptionZh;

    private String descriptionEn;

    private String contactName;

    private String contactPhone;

    private String businessHours;

    private String inviteCode;

    private Integer status;

    private Integer paymentStatus;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentTime;

    private String auditRemark;

    private LocalDateTime auditTime;

    private Long auditorId;

    private String qrCodeUrl;

    private String serviceWechat;

    private String servicePhone;

    private String promotionCode;

    private String promotionLink;

    private String referrerCode;

    private BigDecimal totalReward;

    private BigDecimal availableReward;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

