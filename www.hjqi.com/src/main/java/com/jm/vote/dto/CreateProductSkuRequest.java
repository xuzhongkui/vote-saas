package com.jm.vote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CreateProductSkuRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    private String skuCode;
    
    private String specNameZh;
    
    private String specNameEn;
    
    private Map<String, Object> specValues;
    
    private String image;
    
    private BigDecimal originalPrice;
    
    @NotNull(message = "售价不能为空")
    private BigDecimal price;
    
    @NotNull(message = "库存不能为空")
    private Integer stock = 0;
    
    private Integer weight;
    
    private Integer status = 1;
}

