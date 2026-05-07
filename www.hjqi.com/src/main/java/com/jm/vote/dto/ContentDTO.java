package com.jm.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentDTO {
    private Long id;
    private String contentType;
    private String titleZh;
    private String titleEn;
    private String contentZh;
    private String contentEn;
    private String coverImage;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime publishAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

