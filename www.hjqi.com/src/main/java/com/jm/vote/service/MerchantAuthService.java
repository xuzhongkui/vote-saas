package com.jm.vote.service;

import com.jm.vote.dto.MerchantInfoDTO;
import com.jm.vote.dto.MerchantLoginRequest;
import com.jm.vote.dto.MerchantLoginResponse;
import com.jm.vote.dto.MerchantRegisterRequest;
import com.jm.vote.entity.Merchant;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantAuthService {

    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PromotionService promotionService;
    private final SystemConfigService systemConfigService;
    private final QrCodeService qrCodeService;
    private final MerchantNotificationService merchantNotificationService;
    private final MerchantAuditService merchantAuditService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public Map<String, Object> register(MerchantRegisterRequest request) {
        // 1. 检查用户名是否已存在
        Long count = merchantMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUsername, request.getUsername())
                        .eq(Merchant::getDeleted, 0)
        );
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家账号已存在");
        }

        // 2. 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            count = merchantMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                            .eq(Merchant::getEmail, request.getEmail())
                            .eq(Merchant::getDeleted, 0)
            );
            if (count != null && count > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱已被使用");
            }
        }

        // 3. 验证推荐人推广码（如果提供）
        String referrerCode = request.getReferrerCode();
        if (referrerCode != null && !referrerCode.isEmpty()) {
            Merchant referrer = merchantMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                            .eq(Merchant::getPromotionCode, referrerCode)
                            .eq(Merchant::getDeleted, 0)
            );
            if (referrer == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "推广码无效");
            }
        }

        // 4. 获取系统配置
        boolean auditEnabled = systemConfigService.isMerchantAuditEnabled();
        boolean feeEnabled = systemConfigService.isMerchantRegistrationFeeEnabled();
        BigDecimal registrationFee = systemConfigService.getMerchantRegistrationFee();

        // 5. 创建商家账号
        Merchant merchant = new Merchant();
        merchant.setUsername(request.getUsername());
        merchant.setPassword(passwordEncoder.encode(request.getPassword()));
        merchant.setEmail(request.getEmail());
        merchant.setPhone(request.getPhone());
        merchant.setShopNameZh(request.getShopNameZh());
        merchant.setShopNameEn(request.getShopNameEn());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setReferrerCode(referrerCode);
        
        // 生成邀请码和推广码
        merchant.setInviteCode(generateInviteCode());
        merchant.setPromotionCode(generatePromotionCode());
        
        // 生成推广链接
        merchant.setPromotionLink(baseUrl + "/merchant/register?referrer=" + merchant.getPromotionCode());
        
        // 设置状态：如果关闭审核，直接通过；否则待审核
        if (!auditEnabled) {
            merchant.setStatus(1); // 已通过
            merchant.setAuditTime(LocalDateTime.now());
            merchant.setAuditRemark("自动审核通过");
        } else {
            merchant.setStatus(0); // 待审核
        }
        
        // 设置缴费状态
        if (feeEnabled) {
            merchant.setPaymentStatus(0); // 未缴费
            merchant.setPaymentAmount(registrationFee);
        } else {
            merchant.setPaymentStatus(1); // 无需缴费，标记为已缴费
            merchant.setPaymentAmount(BigDecimal.ZERO);
            merchant.setPaymentTime(LocalDateTime.now());
        }
        
        // 初始化奖励金额
        merchant.setTotalReward(BigDecimal.ZERO);
        merchant.setAvailableReward(BigDecimal.ZERO);
        
        merchant.setDeleted(0);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());

        merchantMapper.insert(merchant);
        
        log.info("商家注册成功: id={}, username={}, auditEnabled={}, feeEnabled={}", 
                merchant.getId(), merchant.getUsername(), auditEnabled, feeEnabled);

        // 6. 生成二维码（如果配置开启）
        if (systemConfigService.isQrCodeAutoGenerate()) {
            try {
                String qrCodeUrl = qrCodeService.generateAndSaveMerchantInviteQrCode(merchant.getId());
                merchant.setQrCodeUrl(qrCodeUrl);
                merchantMapper.updateById(merchant);
            } catch (Exception e) {
                log.error("生成二维码失败: {}", e.getMessage(), e);
            }
        }

        // 7. 推广关系将在缴费完成后记录（不在注册时记录）
        // 只保存推荐人推广码，缴费完成后再记录推广关系并发放奖励

        // 8. 如果审核关闭且无需缴费，直接完成入驻流程
        if (!auditEnabled && !feeEnabled) {
            try {
                merchantAuditService.completeRegistration(merchant);
            } catch (Exception e) {
                log.error("完成入驻流程失败: {}", e.getMessage(), e);
            }
        }

        // 9. 返回注册结果
        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchant.getId());
        result.put("username", merchant.getUsername());
        result.put("inviteCode", merchant.getInviteCode());
        result.put("promotionCode", merchant.getPromotionCode());
        result.put("promotionLink", merchant.getPromotionLink());
        result.put("qrCodeUrl", merchant.getQrCodeUrl());
        result.put("status", merchant.getStatus());
        result.put("paymentStatus", merchant.getPaymentStatus());
        result.put("needAudit", auditEnabled);
        result.put("needPayment", feeEnabled && merchant.getPaymentStatus() == 0);
        result.put("paymentAmount", merchant.getPaymentAmount());
        
        if (!auditEnabled && !feeEnabled) {
            result.put("message", "注册成功！您的账号已激活，可以立即登录使用。");
        } else if (!auditEnabled && feeEnabled) {
            result.put("message", "注册成功！请完成缴费以激活您的账号。");
        } else if (auditEnabled && !feeEnabled) {
            result.put("message", "注册成功！请等待平台审核。");
        } else {
            result.put("message", "注册成功！请等待平台审核，审核通过后需完成缴费。");
        }
        
        return result;
    }

    public MerchantLoginResponse login(MerchantLoginRequest request) {
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUsername, request.getUsername())
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), merchant.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            // 0-待审核 1-已通过 2-已拒绝 3-已禁用
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "商家未通过审核或已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "MERCHANT");
        claims.put("merchantId", merchant.getId());

        String token = jwtUtil.generateToken(merchant.getUsername(), claims);
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpirationMillis();

        MerchantInfoDTO info = new MerchantInfoDTO();
        info.setId(merchant.getId());
        info.setUsername(merchant.getUsername());
        info.setEmail(merchant.getEmail());
        info.setPhone(merchant.getPhone());
        info.setShopNameZh(merchant.getShopNameZh());
        info.setShopNameEn(merchant.getShopNameEn());
        info.setLogo(merchant.getLogo());
        info.setBanner(merchant.getBanner());
        info.setContactName(merchant.getContactName());
        info.setContactPhone(merchant.getContactPhone());
        info.setInviteCode(merchant.getInviteCode());
        info.setPromotionCode(merchant.getPromotionCode());
        // 使用 PromotionService 生成最新的推广链接，确保使用最新的 baseUrl
        info.setPromotionLink(promotionService.generatePromotionLink(merchant.getId()));
        info.setPaymentStatus(merchant.getPaymentStatus());
        info.setPaymentAmount(merchant.getPaymentAmount());
        info.setPaymentTime(merchant.getPaymentTime());
        info.setStatus(merchant.getStatus());
        info.setServiceWechat(merchant.getServiceWechat());
        info.setServicePhone(merchant.getServicePhone());
        info.setBusinessHours(merchant.getBusinessHours());
        info.setTotalReward(merchant.getTotalReward() != null ? merchant.getTotalReward() : BigDecimal.ZERO);
        info.setAvailableReward(merchant.getAvailableReward() != null ? merchant.getAvailableReward() : BigDecimal.ZERO);
        
        MerchantLoginResponse resp = new MerchantLoginResponse();
        resp.setToken(token);
        resp.setExpireAt(expireAt);
        resp.setMerchant(info);
        return resp;
    }

    private String generateInviteCode() {
        // 生成一个 8 位邀请码（大写字母+数字），用于用户注册时绑定商家
        String code;
        do {
            String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = raw.substring(0, 8);
        } while (isInviteCodeExists(code));
        return code;
    }

    private String generatePromotionCode() {
        // 生成一个推广码（M开头+7位大写字母数字），用于商家推广
        String code;
        do {
            String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = "M" + raw.substring(0, 7);
        } while (isPromotionCodeExists(code));
        return code;
    }

    private boolean isInviteCodeExists(String inviteCode) {
        Long count = merchantMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getInviteCode, inviteCode)
                        .eq(Merchant::getDeleted, 0)
        );
        return count != null && count > 0;
    }

    private boolean isPromotionCodeExists(String promotionCode) {
        Long count = merchantMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getPromotionCode, promotionCode)
                        .eq(Merchant::getDeleted, 0)
        );
        return count != null && count > 0;
    }
}


