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
@TableName("t_merchant_bill")
public class MerchantBill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String billNo;

    private String billType; // SERVICE_FEE/PLATFORM_FEE

    private BigDecimal amount;

    private Integer status; // 0-待支付 1-已支付 2-已取消

    private LocalDate startDate;

    private LocalDate endDate;

    private String invoiceUrl;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

