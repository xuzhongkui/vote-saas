package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;
    
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String detailAddress;
    
    private String fullAddress;
    
    private String postalCode;
    
    private Boolean isDefault = false;
    
    private String tag; // 家/公司/学校等
}

