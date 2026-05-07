package com.jm.vote.dto;

import lombok.Data;

@Data
public class BindMerchantResponse {
    
    private String token;
    
    private UserInfoDTO userInfo;
}

