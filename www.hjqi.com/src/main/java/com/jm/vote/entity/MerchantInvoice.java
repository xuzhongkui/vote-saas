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
@TableName("t_merchant_invoice")
public class MerchantInvoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String invoiceNo;

    private String invoiceType;

    private BigDecimal amount;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String invoiceTitle;

    private String taxNumber;

    private LocalDate invoiceDate;

    private String pdfUrl;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

