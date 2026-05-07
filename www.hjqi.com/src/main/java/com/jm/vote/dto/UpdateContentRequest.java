package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateContentRequest {
    private String titleZh;
    private String titleEn;
    private String contentZh;
    private String contentEn;
    private String coverImage;
    private String linkUrl;
    private Integer sort;
    private Integer status;
}

