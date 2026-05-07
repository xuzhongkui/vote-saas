package com.jm.vote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String nickname;

    private String avatar;

    /**
     * SUPER_ADMIN / ADMIN / OPERATOR
     */
    private String role;

    /**
     * 0-禁用 1-正常
     */
    private Integer status;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;

    public boolean isEnabled() {
        return deleted != null && deleted == 0 && status != null && status == 1;
    }
}

