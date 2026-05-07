package com.jm.vote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "商品名称（中文）不能为空")
    private String nameZh;
    
    private String subtitleZh;
    
    private Long categoryId;
    
    private Long brandId;
    
    private BigDecimal originalPrice;
    
    // 如果商品有SKU，价格由SKU决定，此处可以为空
    private BigDecimal price;
    
    // 如果商品有SKU，库存由SKU决定，此处可以为空
    private Integer stock = 0;
    
    private String mainImage;
    
    private List<String> images;
    
    private String descriptionZh;
    
    private Long shippingTemplateId;
    
    private Long afterSaleRuleId;
    
    private Integer status = 1;
}

