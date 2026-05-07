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
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long categoryId;

    private Long brandId;

    private String nameZh;

    private String nameEn;

    private String subtitleZh;

    private String subtitleEn;

    private String mainImage;

    /**
     * JSON 数组字符串
     */
    private String images;

    private String video;

    private String descriptionZh;

    private String descriptionEn;

    private BigDecimal originalPrice;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private String unitZh;

    private String unitEn;

    private Integer weight;

    private Long shippingTemplateId;

    private Long afterSaleRuleId;

    private Integer status;

    private Integer sort;

    private Boolean isRecommend;

    private Boolean isNew;

    private Boolean isHot;

    private Integer limitCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}


