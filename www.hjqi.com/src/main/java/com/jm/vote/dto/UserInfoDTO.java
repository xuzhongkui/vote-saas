package com.jm.vote.dto;

import lombok.Data;

@Data
public class UserInfoDTO {

    private Long id;

    private String username;

    private String nickname;

    private String realName;

    private String avatar;

    private String email;

    private String phone;

    private Long merchantId;

    private String language;
}


