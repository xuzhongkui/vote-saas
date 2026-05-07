package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private Long parentId = 0L;
    
    @NotBlank(message = "分类名称（中文）不能为空")
    private String nameZh;
    
    private String nameEn;
    
    private String icon;
    
    private Integer sort = 0;
    
    private Integer status = 1;
}

