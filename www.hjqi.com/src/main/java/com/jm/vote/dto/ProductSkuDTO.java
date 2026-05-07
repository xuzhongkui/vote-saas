package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ProductSkuDTO {
    private Long id;
    private Long productId;
    private String skuCode;
    private String skuName; // SKU显示名称（优先使用specNameZh，否则使用specNameEn）
    private String specNameZh;
    private String specNameEn;
    private Map<String, Object> specValues;
    private String image;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private Integer weight;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

