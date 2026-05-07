package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String realName;
    private String avatar;
    private String email;
    private String phone;
    private String language; // zh/en
}

