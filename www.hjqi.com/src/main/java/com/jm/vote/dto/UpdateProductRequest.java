package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String nameZh;
    private String subtitleZh;
    private Long categoryId;
    private Long brandId;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer stock;
    private String mainImage;
    private List<String> images;
    private String descriptionZh;
    private Long shippingTemplateId;
    private Long afterSaleRuleId;
    private Integer status;
}

