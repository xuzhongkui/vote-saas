package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long productId;

    private String skuCode;

    private String specNameZh;

    private String specNameEn;

    /**
     * JSON 字符串
     */
    private String specValues;

    private String image;

    private BigDecimal originalPrice;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private Integer weight;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}


