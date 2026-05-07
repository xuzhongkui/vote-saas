package com.jm.vote.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String username;

    private String password;

    private String email;

    private String phone;

    private String nickname;

    /**
     * 商家邀请码，必填，用于绑定 merchant_id
     */
    private String inviteCode;

    /**
     * 推广码（可选），用于记录推广关系
     */
    private String promotionCode;

    /**
     * 语言偏好 zh/en
     */
    private String language;
}


