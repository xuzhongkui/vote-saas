package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {

    private Long id;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private Boolean selected;

    // Snapshot for display
    private String productNameZh;

    private String productNameEn;

    private String productImage;

    private String specNameZh;

    private String specNameEn;

    private BigDecimal price;
}


