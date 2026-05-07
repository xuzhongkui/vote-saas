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
@TableName("t_cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long merchantId;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private Boolean selected;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}


