package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端提现申请 DTO
 */
@Data
public class PromotionWithdrawalDTO {

    private Long id;

    private Long merchantId;

    /** 商家店铺名称（优先中文，其次英文） */
    private String merchantName;

    private String withdrawalNo;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal actualAmount;

    private String accountType;

    private String accountName;

    private String accountNumber;

    /** 状态：0-待审核 1-审核通过 2-已打款 3-已拒绝 */
    private Integer status;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;

    private LocalDateTime transferTime;

    private String transferVoucher;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


