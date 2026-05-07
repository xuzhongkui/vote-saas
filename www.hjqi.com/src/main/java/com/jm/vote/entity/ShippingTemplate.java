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
@TableName("t_shipping_template")
public class ShippingTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String name;

    private String type;

    private BigDecimal freeShippingAmount;

    private String freeShippingCondition;

    private BigDecimal defaultFee;

    private Integer status;

    private Integer isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

