package com.jm.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailDTO {

    private Long id;

    private Long categoryId;

    private Long brandId;

    private String nameZh;

    private String nameEn;

    private String subtitleZh;

    private String subtitleEn;

    private String mainImage;

    private List<String> images;

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
    
    private ShippingTemplateInfo shippingTemplate;

    private Long afterSaleRuleId;
    
    private AfterSaleRuleInfo afterSaleRule;

    private Integer status;

    private Integer sort;

    private Boolean isRecommend;

    private Boolean isNew;

    private Boolean isHot;

    private Integer limitCount;

    private List<ProductSkuDTO> skus;
    
    @Data
    public static class ShippingTemplateInfo {
        private Long id;
        private String name;
        private String type;
        private BigDecimal freeShippingAmount;
        private String freeShippingCondition;
        private BigDecimal defaultFee;
        private List<ShippingTemplateRuleInfo> rules;
    }
    
    @Data
    public static class ShippingTemplateRuleInfo {
        private String regionNames;
        private BigDecimal firstFee;
        private BigDecimal continueFee;
        private Integer firstWeight;
        private Integer continueWeight;
    }
    
    @Data
    public static class AfterSaleRuleInfo {
        private Long id;
        private String titleZh;
        private String titleEn;
        private String contentZh;
        private String contentEn;
        private Integer validDays;
        private String ruleType;
    }
}


