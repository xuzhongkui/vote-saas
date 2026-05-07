package com.jm.vote.dto;

import lombok.Data;

@Data
public class MerchantLoginResponse {

    private String token;

    private long expireAt;

    private MerchantInfoDTO merchant;
}


