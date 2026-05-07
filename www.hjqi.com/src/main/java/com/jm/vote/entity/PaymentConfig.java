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
@TableName("t_payment_config")
public class PaymentConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String paymentType;

    private String configKey;

    private String configValue;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

