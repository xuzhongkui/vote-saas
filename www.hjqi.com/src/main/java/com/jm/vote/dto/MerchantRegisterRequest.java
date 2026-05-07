package com.jm.vote.dto;

import lombok.Data;

@Data
public class MerchantRegisterRequest {

    private String username;

    private String password;

    private String email;

    private String phone;

    private String shopNameZh;

    private String shopNameEn;

    private String contactName;

    private String contactPhone;

    private String referrerCode; // 推荐人推广码（可选）
}


