package com.jm.vote.service;

import com.jm.vote.dto.*;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.User;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.UserMapper;
import com.jm.vote.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PromotionService promotionService;

    public void register(UserRegisterRequest request) {
        // 校验用户名唯一
        Long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        // 用户注册时必须提供邀请码
        if (request.getInviteCode() == null || request.getInviteCode().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家邀请码不能为空，请先绑定商家");
        }

        Merchant merchant = findMerchantByInviteCodeOrThrow(request.getInviteCode());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setMerchantId(merchant.getId());
        user.setStatus(1);
        user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "zh");
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        
        // 记录推广关系（如果通过推广码注册）
        if (request.getPromotionCode() != null && !request.getPromotionCode().isEmpty()) {
            try {
                Merchant promoter = merchantMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                                .eq(Merchant::getPromotionCode, request.getPromotionCode())
                                .eq(Merchant::getDeleted, 0)
                );
                if (promoter != null) {
                    promotionService.recordUserPromotion(promoter.getId(), user.getId());
                }
            } catch (Exception e) {
                // 推广记录失败不影响注册
            }
        }
    }

    public UserLoginResponse login(UserLoginRequest request) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getDeleted, 0)
        );
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "账号已禁用");
        }

        // 首次登录时必须绑定商家
        if (user.getMerchantId() == null) {
            if (request.getInviteCode() != null && !request.getInviteCode().isEmpty()) {
                Merchant merchant = findMerchantByInviteCodeOrThrow(request.getInviteCode());
                user.setMerchantId(merchant.getId());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            } else {
                // 如果没有提供邀请码，拒绝登录，要求先绑定商家
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "首次登录必须绑定商家，请在登录时提供商家邀请码，或注册时已绑定商家");
            }
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "USER");
        claims.put("userId", user.getId());
        if (user.getMerchantId() != null) {
            claims.put("merchantId", user.getMerchantId());
        }

        String token = jwtUtil.generateToken(user.getUsername(), claims);
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpirationMillis();

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setMerchantId(user.getMerchantId());
        userInfo.setLanguage(user.getLanguage());

        UserLoginResponse resp = new UserLoginResponse();
        resp.setToken(token);
        resp.setExpireAt(expireAt);
        resp.setUser(userInfo);

        return resp;
    }

    private Merchant findMerchantByInviteCodeOrThrow(String inviteCode) {
        if (inviteCode == null || inviteCode.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家邀请码不能为空");
        }
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getInviteCode, inviteCode)
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效或未审核通过的商家邀请码");
        }
        return merchant;
    }
}


