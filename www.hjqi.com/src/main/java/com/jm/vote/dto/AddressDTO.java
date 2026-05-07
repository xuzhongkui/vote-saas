package com.jm.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressDTO {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String fullAddress;
    private String postalCode;
    private Boolean isDefault;
    private String tag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

