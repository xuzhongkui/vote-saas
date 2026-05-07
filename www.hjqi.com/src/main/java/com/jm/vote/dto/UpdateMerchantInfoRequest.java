package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateMerchantInfoRequest {
    private String shopNameZh;
    private String shopNameEn;
    private String logo;
    private String banner;
    private String descriptionZh;
    private String descriptionEn;
    private String contactName;
    private String contactPhone;
    private String businessHours;
    private String serviceWechat;
    private String servicePhone;
}

