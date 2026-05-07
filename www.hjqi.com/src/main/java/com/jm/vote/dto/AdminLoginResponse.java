package com.jm.vote.dto;

import lombok.Data;

@Data
public class AdminLoginResponse {

    private String token;

    /**
     * 过期时间戳（毫秒）
     */
    private long expireAt;

    private AdminInfoDTO admin;
}


