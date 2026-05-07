package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private String merchantRemark;
    private String cancelReason;
}

