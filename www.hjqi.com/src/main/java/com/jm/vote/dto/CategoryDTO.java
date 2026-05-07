package com.jm.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDTO {

    private Long id;

    private Long parentId;

    private String nameZh;

    private String nameEn;

    private String icon;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdAt;
}


