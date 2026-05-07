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
@TableName("t_shipping_template_rule")
public class ShippingTemplateRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private String regionCodes;

    private String regionNames;

    private Integer firstWeight;

    private BigDecimal firstFee;

    private Integer continueWeight;

    private BigDecimal continueFee;

    private Integer firstCount;

    private BigDecimal firstCountFee;

    private Integer continueCount;

    private BigDecimal continueCountFee;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

