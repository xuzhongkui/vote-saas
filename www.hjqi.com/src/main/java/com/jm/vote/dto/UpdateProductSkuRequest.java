package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UpdateProductSkuRequest {
    private String skuCode;
    private String specNameZh;
    private String specNameEn;
    private Map<String, Object> specValues;
    private String image;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer stock;
    private Integer weight;
    private Integer status;
}

