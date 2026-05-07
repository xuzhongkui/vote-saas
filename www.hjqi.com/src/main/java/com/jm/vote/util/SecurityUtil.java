package com.jm.vote.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.Admin;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.User;
import com.jm.vote.repository.AdminMapper;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    /**
     * 获取当前登录的商家ID
     * 如果是商家登录，返回商家自己的ID
     * 如果是用户登录，返回用户绑定的商家ID
     */
    public Long getCurrentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        
        // 先尝试从商家表查询（商家登录）
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUsername, username)
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant != null) {
            return merchant.getId();
        }
        
        // 如果不是商家，尝试从用户表查询用户绑定的商家ID
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );
        return user != null ? user.getMerchantId() : null;
    }

    /**
     * 获取当前登录的用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录的用户信息
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleted, 0)
        );
    }

    /**
     * 获取当前登录的管理员ID
     */
    public Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getDeleted, 0)
        );
        return admin != null ? admin.getId() : null;
    }

    /**
     * 获取当前登录的管理员信息
     */
    public Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        return adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getDeleted, 0)
        );
    }

    /**
     * 要求当前用户必须是管理员，否则抛出异常
     * @return 管理员ID
     * @throws RuntimeException 如果当前用户不是管理员
     */
    public Long requireAdmin() {
        Long adminId = getCurrentAdminId();
        if (adminId == null) {
            throw new RuntimeException("需要管理员权限");
        }
        return adminId;
    }

    /**
     * 要求当前用户必须是商家，否则抛出异常
     * @return 商家ID
     * @throws RuntimeException 如果当前用户不是商家
     */
    public Long requireMerchant() {
        Long merchantId = getCurrentMerchantId();
        if (merchantId == null) {
            throw new RuntimeException("需要商家权限");
        }
        return merchantId;
    }

    /**
     * 要求当前用户必须是普通用户，否则抛出异常
     * @return 用户ID
     * @throws RuntimeException 如果当前用户不是普通用户
     */
    public Long requireUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("需要用户权限");
        }
        return userId;
    }
}

