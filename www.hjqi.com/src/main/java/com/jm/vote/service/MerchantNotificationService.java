package com.jm.vote.service;

import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.MerchantInvoice;
import com.jm.vote.repository.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantNotificationService {

    private final EmailService emailService;
    private final MerchantMapper merchantMapper;
    private final SystemConfigService systemConfigService;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 发送商家注册成功通知（审核关闭时）
     */
    public void sendRegistrationSuccessNotification(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            // 获取推广奖励配置
            String merchantReward = systemConfigService.getMerchantPromotionReward().toString();
            String userRewardRate = systemConfigService.getUserPromotionRewardRate().toString();

            // 发送欢迎邮件
            emailService.sendMerchantWelcomeEmail(
                    merchant.getEmail(),
                    merchant.getUsername(),
                    merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername(),
                    merchant.getInviteCode(),
                    merchant.getPromotionLink(),
                    merchantReward,
                    userRewardRate,
                    "zh"
            );

            log.info("商家注册成功通知邮件已发送: merchantId={}, email={}", merchantId, merchant.getEmail());
        } catch (Exception e) {
            log.error("发送商家注册成功通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送审核通过通知（需要缴费）
     */
    public void sendAuditApprovedWithPaymentNotification(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            String fee = merchant.getPaymentAmount() != null 
                    ? merchant.getPaymentAmount().toString() 
                    : systemConfigService.getMerchantRegistrationFee().toString();
            String paymentLink = baseUrl + "/merchant/payment?merchantId=" + merchantId;

            emailService.sendMerchantAuditApprovedEmail(
                    merchant.getEmail(),
                    merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername(),
                    fee,
                    paymentLink,
                    "zh"
            );

            log.info("审核通过通知邮件已发送（需缴费）: merchantId={}, email={}", merchantId, merchant.getEmail());
        } catch (Exception e) {
            log.error("发送审核通过通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送审核通过通知（自动判断是否需要缴费）
     */
    public void sendAuditApprovedNotification(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            // 检查是否需要缴费
            boolean needPayment = systemConfigService.isMerchantRegistrationFeeEnabled() 
                    && (merchant.getPaymentStatus() == null || merchant.getPaymentStatus() == 0);

            if (needPayment) {
                // 需要缴费，发送审核通过+缴费通知
                String fee = merchant.getPaymentAmount() != null 
                        ? merchant.getPaymentAmount().toString() 
                        : systemConfigService.getMerchantRegistrationFee().toString();
                String paymentLink = baseUrl + "/merchant/payment";

                emailService.sendMerchantAuditApprovedEmail(
                        merchant.getEmail(),
                        merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername(),
                        fee,
                        paymentLink,
                        "zh"
                );

                log.info("审核通过通知邮件已发送（需缴费）: merchantId={}, email={}", merchantId, merchant.getEmail());
            } else {
                // 无需缴费，直接发送欢迎邮件
                sendRegistrationSuccessNotification(merchantId);
            }
        } catch (Exception e) {
            log.error("发送审核通过通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送审核拒绝通知
     */
    public void sendAuditRejectedNotification(Long merchantId, String reason) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            emailService.sendMerchantAuditRejectedEmail(
                    merchant.getEmail(),
                    merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername(),
                    reason != null ? reason : "未通过审核",
                    "zh"
            );

            log.info("审核拒绝通知邮件已发送: merchantId={}, email={}", merchantId, merchant.getEmail());
        } catch (Exception e) {
            log.error("发送审核拒绝通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送缴费成功通知（包含发票）
     */
    public void sendPaymentSuccessNotification(Long merchantId, MerchantInvoice invoice) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            // 如果还没发送过欢迎邮件，先发送欢迎邮件
            if (systemConfigService.isWelcomeEmailEnabled()) {
                sendRegistrationSuccessNotification(merchantId);
            }

            // 如果有发票，发送发票邮件
            if (invoice != null && invoice.getPdfUrl() != null) {
                sendInvoiceNotification(merchantId, invoice);
            }
        } catch (Exception e) {
            log.error("发送缴费成功通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送发票通知邮件
     */
    public void sendInvoiceNotification(Long merchantId, MerchantInvoice invoice) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过发票邮件发送: merchantId={}", merchantId);
                return;
            }

            if (invoice != null && invoice.getPdfUrl() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String downloadUrl = baseUrl + invoice.getPdfUrl();

                emailService.sendMerchantInvoiceEmail(
                        merchant.getEmail(),
                        merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername(),
                        invoice.getInvoiceNo(),
                        invoice.getInvoiceDate().format(dateFormatter),
                        invoice.getTotalAmount().toString(),
                        downloadUrl,
                        "zh"
                );

                log.info("发票邮件已发送: merchantId={}, email={}, invoiceNo={}", 
                        merchantId, merchant.getEmail(), invoice.getInvoiceNo());
            }
        } catch (Exception e) {
            log.error("发送发票通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }

    /**
     * 发送完整的入驻成功通知（审核通过+缴费完成）
     */
    public void sendCompleteRegistrationNotification(Long merchantId) {
        try {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant == null || merchant.getEmail() == null) {
                log.warn("商家不存在或邮箱为空，跳过邮件发送: merchantId={}", merchantId);
                return;
            }

            // 检查是否审核通过且已缴费
            boolean isApproved = merchant.getStatus() != null && merchant.getStatus() == 1;
            boolean isPaid = merchant.getPaymentStatus() != null && merchant.getPaymentStatus() == 1;

            if (isApproved && isPaid) {
                sendRegistrationSuccessNotification(merchantId);
                log.info("完整入驻成功通知已发送: merchantId={}", merchantId);
            }
        } catch (Exception e) {
            log.error("发送完整入驻成功通知失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
        }
    }
}

