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
@TableName("t_password_reset_token")
public class PasswordResetToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String phone;

    private String token;

    private String code; // 验证码

    private String userType; // USER/MERCHANT/ADMIN

    private Integer used;

    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}

