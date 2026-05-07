package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.MerchantInvoice;
import com.jm.vote.entity.PaymentOrder;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantPaymentService {

    private final MerchantMapper merchantMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final SystemConfigService systemConfigService;
    private final InvoiceService invoiceService;
    private final MerchantAuditService merchantAuditService;
    private final MerchantNotificationService merchantNotificationService;
    private final PromotionService promotionService;

    /**
     * 创建商家入驻缴费订单
     */
    @Transactional
    public Map<String, Object> createRegistrationPaymentOrder(Long merchantId) {
        // 1. 查询商家信息
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }

        // 2. 检查商家状态
        if (merchant.getStatus() == null || merchant.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商家审核未通过，无法缴费");
        }

        // 3. 检查是否已缴费
        if (merchant.getPaymentStatus() != null && merchant.getPaymentStatus() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已完成缴费，无需重复支付");
        }

        // 4. 检查是否已有未支付的订单
        PaymentOrder existingOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getMerchantId, merchantId)
                        .eq(PaymentOrder::getOrderType, "MERCHANT_REG")
                        .eq(PaymentOrder::getStatus, "PENDING")
                        .orderByDesc(PaymentOrder::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (existingOrder != null) {
            // 返回现有订单
            Map<String, Object> result = new HashMap<>();
            result.put("paymentNo", existingOrder.getPaymentNo());
            result.put("amount", existingOrder.getAmount());
            result.put("status", existingOrder.getStatus());
            result.put("createdAt", existingOrder.getCreatedAt());
            return result;
        }

        // 5. 获取缴费金额
        BigDecimal amount = merchant.getPaymentAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = systemConfigService.getMerchantRegistrationFee();
        }

        // 6. 创建支付订单
        PaymentOrder paymentOrder = new PaymentOrder();
        // 入驻缴费不关联具体业务订单，避免数据库非空约束，统一填0
        paymentOrder.setOrderId(0L);
        paymentOrder.setMerchantId(merchantId);
        paymentOrder.setOrderType("MERCHANT_REG");
        paymentOrder.setPaymentNo(generatePaymentNo());
        paymentOrder.setPaymentType("ALIPAY"); // 默认支付方式，后续支付时可修改
        paymentOrder.setAmount(amount);
        paymentOrder.setStatus("PENDING");
        paymentOrder.setDescription("商家入驻费用 - " + merchant.getShopNameZh());
        paymentOrder.setCreatedAt(LocalDateTime.now());
        paymentOrder.setUpdatedAt(LocalDateTime.now());

        paymentOrderMapper.insert(paymentOrder);

        log.info("创建商家入驻缴费订单: merchantId={}, paymentNo={}, amount={}", 
                merchantId, paymentOrder.getPaymentNo(), amount);

        // 7. 返回订单信息
        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentOrder.getPaymentNo());
        result.put("amount", amount);
        result.put("status", "PENDING");
        result.put("createdAt", paymentOrder.getCreatedAt());
        result.put("merchantName", merchant.getShopNameZh());
        result.put("description", paymentOrder.getDescription());

        return result;
    }

    /**
     * 处理支付回调
     */
    @Transactional
    public void handlePaymentCallback(String paymentNo, String thirdPartyOrderNo, String callbackData) {
        // 1. 查询支付订单
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getPaymentNo, paymentNo)
        );

        if (paymentOrder == null) {
            log.error("支付订单不存在: paymentNo={}", paymentNo);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在");
        }

        // 2. 检查订单状态
        if ("SUCCESS".equals(paymentOrder.getStatus())) {
            log.warn("订单已支付，忽略重复回调: paymentNo={}", paymentNo);
            return;
        }

        // 3. 更新支付订单状态
        paymentOrder.setStatus("SUCCESS");
        paymentOrder.setThirdPartyOrderNo(thirdPartyOrderNo);
        paymentOrder.setCallbackData(callbackData);
        paymentOrder.setPaidAt(LocalDateTime.now());
        paymentOrder.setUpdatedAt(LocalDateTime.now());
        paymentOrderMapper.updateById(paymentOrder);

        log.info("支付订单状态更新成功: paymentNo={}, status=SUCCESS", paymentNo);

        // 4. 如果是商家入驻订单，更新商家缴费状态
        if ("MERCHANT_REG".equals(paymentOrder.getOrderType()) && paymentOrder.getMerchantId() != null) {
            updateMerchantPaymentStatus(paymentOrder.getMerchantId(), paymentOrder.getAmount());
        }
    }

    /**
     * 更新商家缴费状态
     */
    @Transactional
    public void updateMerchantPaymentStatus(Long merchantId, BigDecimal amount) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.error("商家不存在: merchantId={}", merchantId);
            return;
        }

        merchant.setPaymentStatus(1); // 已缴费
        merchant.setPaymentAmount(amount);
        merchant.setPaymentTime(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        log.info("商家缴费状态更新成功: merchantId={}, paymentStatus=1, amount={}", 
                merchantId, amount);

        // 如果审核已通过且已缴费，完成入驻流程
        // 注意：completeRegistration() 内部会发送欢迎邮件，无需在此重复发送
        if (merchant.getStatus() != null && merchant.getStatus() == 1) {
            try {
                merchantAuditService.completeRegistration(merchant);
                log.info("商家入驻流程已完成: merchantId={}", merchantId);
            } catch (Exception e) {
                log.error("完成入驻流程失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
            }
        }

        // 记录推广关系并发放奖励（如果填写了推广码）
        // 注意：completeRegistration() 内部也会处理推广关系，这里做双重保障
        if (merchant.getReferrerCode() != null && !merchant.getReferrerCode().isEmpty()) {
            try {
                promotionService.recordMerchantPromotionByCode(merchant.getReferrerCode(), merchantId);
                // 确认推广奖励（缴费完成后立即确认）
                promotionService.confirmMerchantPromotionReward(merchantId);
                log.info("推广关系已记录并奖励已发放: merchantId={}, referrerCode={}", merchantId, merchant.getReferrerCode());
            } catch (Exception e) {
                log.error("记录推广关系或发放奖励失败: merchantId={}, referrerCode={}, error={}", 
                        merchantId, merchant.getReferrerCode(), e.getMessage(), e);
            }
        }

        // 自动生成发票（如果配置开启）
        // 注意：InvoiceService.generateRegistrationInvoice() 内部会自动发送发票邮件，无需重复发送
        if (systemConfigService.isInvoiceAutoGenerate()) {
            try {
                MerchantInvoice invoice = invoiceService.generateRegistrationInvoice(merchantId);
                log.info("自动生成发票成功: merchantId={}, invoiceNo={}", merchantId, 
                        invoice != null ? invoice.getInvoiceNo() : "null");
            } catch (Exception e) {
                log.error("自动生成发票失败: merchantId={}, error={}", merchantId, e.getMessage(), e);
            }
        }
    }

    /**
     * 查询支付订单状态
     */
    public Map<String, Object> getPaymentOrderStatus(String paymentNo) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getPaymentNo, paymentNo)
        );

        if (paymentOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentOrder.getPaymentNo());
        result.put("amount", paymentOrder.getAmount());
        result.put("status", paymentOrder.getStatus());
        result.put("orderType", paymentOrder.getOrderType());
        result.put("description", paymentOrder.getDescription());
        result.put("createdAt", paymentOrder.getCreatedAt());
        result.put("paidAt", paymentOrder.getPaidAt());

        return result;
    }

    /**
     * 查询商家的支付订单
     */
    public Map<String, Object> getMerchantPaymentOrder(Long merchantId) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getMerchantId, merchantId)
                        .eq(PaymentOrder::getOrderType, "MERCHANT_REG")
                        .orderByDesc(PaymentOrder::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (paymentOrder == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentNo", paymentOrder.getPaymentNo());
        result.put("amount", paymentOrder.getAmount());
        result.put("status", paymentOrder.getStatus());
        result.put("description", paymentOrder.getDescription());
        result.put("createdAt", paymentOrder.getCreatedAt());
        result.put("paidAt", paymentOrder.getPaidAt());

        return result;
    }

    /**
     * 生成支付订单号
     */
    private String generatePaymentNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "PAY" + timestamp + random;
    }
}

