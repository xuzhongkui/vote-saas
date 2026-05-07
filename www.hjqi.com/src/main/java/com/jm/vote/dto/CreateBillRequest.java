package com.jm.vote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBillRequest {
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;
    
    @NotNull(message = "账单类型不能为空")
    private String billType; // SERVICE_FEE/PLATFORM_FEE
    
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
}

