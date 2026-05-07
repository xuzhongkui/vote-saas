package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Long id;

    private Long productId;

    private Long skuId;

    /**
     * 商品名称（前端使用）
     */
    private String productName;

    private String productNameZh;

    private String productNameEn;

    private String productImage;

    /**
     * 规格名称（前端使用）
     */
    private String skuName;

    private String specNameZh;

    private String specNameEn;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}


