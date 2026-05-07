package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_after_sale_rule")
public class AfterSaleRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long productId;

    private String ruleType;

    private String titleZh;

    private String titleEn;

    private String contentZh;

    private String contentEn;

    private Integer validDays;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}

