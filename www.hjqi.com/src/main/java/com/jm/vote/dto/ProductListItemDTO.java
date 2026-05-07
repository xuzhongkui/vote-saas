package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductListItemDTO {

    private Long id;

    private Long categoryId;

    private String nameZh;

    private String nameEn;

    private String mainImage;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private Integer status;

    private Boolean isRecommend;

    private Boolean isNew;

    private Boolean isHot;
    
    private Boolean hasSkus; // 是否有SKU
    
    private List<ProductSkuDTO> skus; // SKU列表（用于显示最低价格）
}


