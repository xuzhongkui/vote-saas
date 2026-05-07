package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateContentRequest {
    @NotBlank(message = "内容类型不能为空")
    private String contentType; // SEO/ACTIVITY/NEWS/ANNOUNCEMENT
    
    private String titleZh;
    private String titleEn;
    private String contentZh;
    private String contentEn;
    private String coverImage;
    private String linkUrl;
    private Integer sort;
    private Integer status;
}

