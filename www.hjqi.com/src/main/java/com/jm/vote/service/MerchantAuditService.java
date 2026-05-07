package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.Merchant;
import com.jm.vote.repository.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家审核服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantAuditService {

    private final MerchantMapper merchantMapper;
    private final MerchantNotificationService merchantNotificationService;  // 统一使用通知服务发送邮件
    private final SystemConfigService systemConfigService;
    private final QrCodeService qrCodeService;
    private final PromotionService promotionService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 审核通过
     */
    @Transactional
    public Map<String, Object> approve(Long merchantId, Long auditorId, String remark) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        if (merchant.getStatus() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家状态不是待审核");
        }
        
        // 更新商家状态
        merchant.setStatus(1); // 已通过
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setAuditorId(auditorId);
        merchant.setAuditRemark(remark != null ? remark : "审核通过");
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        
        log.info("商家审核通过: merchantId={}, auditorId={}", merchantId, auditorId);
        
        // 检查是否需要缴费
        boolean needPayment = systemConfigService.isMerchantRegistrationFeeEnabled() 
                && merchant.getPaymentStatus() == 0;
        
        if (needPayment) {
            // 发送审核通过邮件（需要缴费）- 通过通知服务发送
            merchantNotificationService.sendAuditApprovedWithPaymentNotification(merchant.getId());
        } else {
            // 无需缴费，直接完成入驻流程
            completeRegistration(merchant);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchantId);
        result.put("status", merchant.getStatus());
        result.put("needPayment", needPayment);
        result.put("message", needPayment ? "审核通过，请商家完成缴费" : "审核通过，商家已激活");
        
        return result;
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public Map<String, Object> reject(Long merchantId, Long auditorId, String reason) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        if (merchant.getStatus() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家状态不是待审核");
        }
        
        // 更新商家状态
        merchant.setStatus(2); // 已拒绝
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setAuditorId(auditorId);
        merchant.setAuditRemark(reason != null ? reason : "审核未通过");
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        
        log.info("商家审核拒绝: merchantId={}, auditorId={}, reason={}", merchantId, auditorId, reason);
        
        // 发送审核拒绝邮件 - 通过通知服务发送
        merchantNotificationService.sendAuditRejectedNotification(merchantId, reason);
        
        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchantId);
        result.put("status", merchant.getStatus());
        result.put("message", "审核已拒绝");
        
        return result;
    }

    /**
     * 完成商家入驻流程（审核通过且缴费完成后调用）
     */
    @Transactional
    public void completeRegistration(Merchant merchant) {
        // 生成二维码
        if (systemConfigService.isQrCodeAutoGenerate()) {
            try {
                String qrCodeUrl = qrCodeService.generateAndSaveMerchantInviteQrCode(merchant.getId());
                merchant.setQrCodeUrl(qrCodeUrl);
                merchantMapper.updateById(merchant);
            } catch (Exception e) {
                log.error("生成二维码失败: merchantId={}, error={}", merchant.getId(), e.getMessage(), e);
            }
        }
        
        // 记录推广关系并确认奖励（如果有推荐人）
        if (merchant.getReferrerCode() != null && !merchant.getReferrerCode().isEmpty()) {
            try {
                // 先记录推广关系
                promotionService.recordMerchantPromotionByCode(merchant.getReferrerCode(), merchant.getId());
                // 然后确认推广奖励
                promotionService.confirmMerchantPromotionReward(merchant.getId());
                log.info("推广关系已记录并奖励已发放: merchantId={}, referrerCode={}", merchant.getId(), merchant.getReferrerCode());
            } catch (Exception e) {
                log.error("记录推广关系或发放奖励失败: merchantId={}, referrerCode={}, error={}", 
                        merchant.getId(), merchant.getReferrerCode(), e.getMessage(), e);
            }
        }
        
        // 发送欢迎邮件（缴费完成后必须发送）- 通过通知服务发送
        try {
            merchantNotificationService.sendRegistrationSuccessNotification(merchant.getId());
            log.info("欢迎邮件已发送: merchantId={}, email={}", merchant.getId(), merchant.getEmail());
        } catch (Exception e) {
            // 邮件发送失败不影响入驻流程完成，只记录日志
            log.error("发送欢迎邮件失败: merchantId={}, error={}", merchant.getId(), e.getMessage(), e);
        }
        
        log.info("商家入驻流程完成: merchantId={}, username={}", merchant.getId(), merchant.getUsername());
    }

    /**
     * 获取待审核商家列表
     */
    public java.util.List<Merchant> getPendingMerchants() {
        return merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, 0)
                        .eq(Merchant::getDeleted, 0)
                        .orderByAsc(Merchant::getCreatedAt)
        );
    }

    /**
     * 获取商家审核详情
     */
    public Map<String, Object> getMerchantAuditDetail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", merchant.getId());
        detail.put("username", merchant.getUsername());
        detail.put("email", merchant.getEmail());
        detail.put("phone", merchant.getPhone());
        detail.put("shopNameZh", merchant.getShopNameZh());
        detail.put("shopNameEn", merchant.getShopNameEn());
        detail.put("contactName", merchant.getContactName());
        detail.put("contactPhone", merchant.getContactPhone());
        detail.put("status", merchant.getStatus());
        detail.put("paymentStatus", merchant.getPaymentStatus());
        detail.put("paymentAmount", merchant.getPaymentAmount());
        detail.put("auditRemark", merchant.getAuditRemark());
        detail.put("auditTime", merchant.getAuditTime());
        detail.put("referrerCode", merchant.getReferrerCode());
        detail.put("createdAt", merchant.getCreatedAt());
        
        return detail;
    }

    /**
     * 批量审核通过
     */
    @Transactional
    public Map<String, Object> batchApprove(java.util.List<Long> merchantIds, Long auditorId, String remark) {
        int successCount = 0;
        int failCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        for (Long merchantId : merchantIds) {
            try {
                approve(merchantId, auditorId, remark);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("商家ID " + merchantId + ": " + e.getMessage());
                log.error("批量审核失败: merchantId={}, error={}", merchantId, e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", merchantIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        result.put("message", String.format("批量审核完成：成功 %d 个，失败 %d 个", successCount, failCount));
        
        return result;
    }

    /**
     * 批量审核拒绝
     */
    @Transactional
    public Map<String, Object> batchReject(java.util.List<Long> merchantIds, Long auditorId, String reason) {
        int successCount = 0;
        int failCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        for (Long merchantId : merchantIds) {
            try {
                reject(merchantId, auditorId, reason);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("商家ID " + merchantId + ": " + e.getMessage());
                log.error("批量审核拒绝失败: merchantId={}, error={}", merchantId, e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", merchantIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        result.put("message", String.format("批量审核完成：成功 %d 个，失败 %d 个", successCount, failCount));
        
        return result;
    }
}

