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
@TableName("t_promotion_withdrawal")
public class PromotionWithdrawal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String withdrawalNo;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal actualAmount;

    private String accountType;

    private String accountName;

    private String accountNumber;

    private Integer status;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;

    private LocalDateTime transferTime;

    private String transferVoucher;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

