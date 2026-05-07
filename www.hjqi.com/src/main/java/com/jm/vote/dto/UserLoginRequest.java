package com.jm.vote.dto;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String username;

    private String password;

    /**
     * 可选：首次登录时用于绑定商家
     */
    private String inviteCode;
}


