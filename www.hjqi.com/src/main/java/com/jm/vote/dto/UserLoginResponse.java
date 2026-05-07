package com.jm.vote.dto;

import lombok.Data;

@Data
public class UserLoginResponse {

    private String token;

    private long expireAt;

    private UserInfoDTO user;
}


