package com.jm.vote.dto;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private Long parentId;
    private String nameZh;
    private String nameEn;
    private String icon;
    private Integer sort;
    private Integer status;
}

