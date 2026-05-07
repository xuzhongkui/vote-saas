package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.MerchantBill;
import com.jm.vote.entity.MerchantInvoice;
import com.jm.vote.entity.PaymentOrder;
import com.jm.vote.repository.MerchantInvoiceMapper;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 发票服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final MerchantInvoiceMapper invoiceMapper;
    private final MerchantMapper merchantMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final EmailService emailService;
    private final SystemConfigService systemConfigService;
    private final PdfService pdfService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 生成商家入驻发票（直接通过商家ID）
     */
    @Transactional
    public MerchantInvoice generateRegistrationInvoice(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        // 检查是否已经生成过发票
        MerchantInvoice existingInvoice = invoiceMapper.selectOne(
                new LambdaQueryWrapper<MerchantInvoice>()
                        .eq(MerchantInvoice::getMerchantId, merchantId)
                        .eq(MerchantInvoice::getInvoiceType, "REGISTRATION")
                        .in(MerchantInvoice::getStatus, 1, 2) // 已生成或已发送
        );
        
        if (existingInvoice != null) {
            log.info("发票已存在: merchantId={}, invoiceNo={}", merchantId, existingInvoice.getInvoiceNo());
            return existingInvoice;
        }
        
        // 查找支付订单
        PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getMerchantId, merchantId)
                        .eq(PaymentOrder::getOrderType, "MERCHANT_REG")
                        .eq(PaymentOrder::getStatus, "SUCCESS")
                        .orderByDesc(PaymentOrder::getPaidAt)
                        .last("LIMIT 1")
        );
        
        BigDecimal amount = merchant.getPaymentAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            if (paymentOrder != null) {
                amount = paymentOrder.getAmount();
            } else {
                amount = systemConfigService.getMerchantRegistrationFee();
            }
        }
        
        // 生成发票
        MerchantInvoice invoice = new MerchantInvoice();
        invoice.setMerchantId(merchantId);
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setInvoiceType("REGISTRATION");
        invoice.setAmount(amount);
        
        // 计算税额
        BigDecimal taxRate = systemConfigService.getInvoiceTaxRate();
        BigDecimal taxAmount = amount
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(amount.add(taxAmount));
        
        invoice.setInvoiceTitle(merchant.getShopNameZh());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus(1); // 已生成
        invoice.setRemark("商家入驻费用发票");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        invoiceMapper.insert(invoice);
        
        // 生成PDF
        try {
            String pdfUrl = pdfService.generateInvoicePdf(invoice, merchant);
            invoice.setPdfUrl(pdfUrl);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
        } catch (Exception e) {
            log.error("生成发票PDF失败: invoiceNo={}, error={}", invoice.getInvoiceNo(), e.getMessage(), e);
            // PDF生成失败不影响发票创建，后续可以重新生成
        }
        
        log.info("商家入驻发票生成成功: merchantId={}, invoiceNo={}", merchantId, invoice.getInvoiceNo());
        
        // 发送发票邮件
        if (systemConfigService.isInvoiceAutoGenerate()) {
            sendInvoiceEmail(invoice, merchant);
        }
        
        return invoice;
    }

    /**
     * 为账单生成发票
     */
    @Transactional
    public MerchantInvoice generateBillInvoice(MerchantBill bill) {
        if (bill == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账单不存在");
        }
        
        Long merchantId = bill.getMerchantId();
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        // 检查是否已经生成过发票
        MerchantInvoice existingInvoice = invoiceMapper.selectOne(
                new LambdaQueryWrapper<MerchantInvoice>()
                        .eq(MerchantInvoice::getMerchantId, merchantId)
                        .eq(MerchantInvoice::getInvoiceType, bill.getBillType())
                        .eq(MerchantInvoice::getRemark, "账单号: " + bill.getBillNo())
                        .in(MerchantInvoice::getStatus, 1, 2) // 已生成或已发送
        );
        
        if (existingInvoice != null) {
            log.info("账单发票已存在: billNo={}, invoiceNo={}", bill.getBillNo(), existingInvoice.getInvoiceNo());
            return existingInvoice;
        }
        
        // 生成发票
        MerchantInvoice invoice = new MerchantInvoice();
        invoice.setMerchantId(merchantId);
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setInvoiceType(bill.getBillType()); // SERVICE_FEE 或 PLATFORM_FEE
        invoice.setAmount(bill.getAmount());
        
        // 计算税额
        BigDecimal taxRate = systemConfigService.getInvoiceTaxRate();
        BigDecimal taxAmount = bill.getAmount()
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(bill.getAmount().add(taxAmount));
        
        invoice.setInvoiceTitle(merchant.getShopNameZh());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus(1); // 已生成
        invoice.setRemark("账单号: " + bill.getBillNo());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        invoiceMapper.insert(invoice);
        
        // 生成PDF
        try {
            String pdfUrl = pdfService.generateInvoicePdf(invoice, merchant);
            invoice.setPdfUrl(pdfUrl);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
        } catch (Exception e) {
            log.error("生成账单发票PDF失败: billNo={}, error={}", bill.getBillNo(), e.getMessage(), e);
            // PDF生成失败不影响发票创建，后续可以重新生成
        }
        
        log.info("账单发票生成成功: billNo={}, invoiceNo={}", bill.getBillNo(), invoice.getInvoiceNo());
        
        // 发送发票邮件
        if (systemConfigService.isInvoiceAutoGenerate()) {
            sendInvoiceEmail(invoice, merchant);
        }
        
        return invoice;
    }

    /**
     * 自动生成发票（支付成功后调用）
     */
    @Transactional
    public MerchantInvoice generateInvoiceForPayment(Long paymentOrderId) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectById(paymentOrderId);
        if (paymentOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "支付订单不存在");
        }
        
        if (!"SUCCESS".equals(paymentOrder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "支付订单未完成");
        }
        
        Long merchantId = paymentOrder.getMerchantId();
        if (merchantId == null) {
            log.warn("支付订单没有关联商家: paymentOrderId={}", paymentOrderId);
            return null;
        }
        
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        // 检查是否已经生成过发票
        MerchantInvoice existingInvoice = invoiceMapper.selectOne(
                new LambdaQueryWrapper<MerchantInvoice>()
                        .eq(MerchantInvoice::getMerchantId, merchantId)
                        .eq(MerchantInvoice::getInvoiceType, "REGISTRATION")
                        .in(MerchantInvoice::getStatus, 1, 2) // 已生成或已发送
        );
        
        if (existingInvoice != null) {
            log.info("发票已存在: merchantId={}, invoiceNo={}", merchantId, existingInvoice.getInvoiceNo());
            return existingInvoice;
        }
        
        // 生成发票
        MerchantInvoice invoice = new MerchantInvoice();
        invoice.setMerchantId(merchantId);
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setInvoiceType("REGISTRATION");
        invoice.setAmount(paymentOrder.getAmount());
        
        // 计算税额
        BigDecimal taxRate = systemConfigService.getInvoiceTaxRate();
        BigDecimal taxAmount = paymentOrder.getAmount()
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(paymentOrder.getAmount().add(taxAmount));
        
        invoice.setInvoiceTitle(merchant.getShopNameZh());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus(1); // 已生成
        invoice.setRemark("商家入驻费用发票");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        invoiceMapper.insert(invoice);
        
        // 生成PDF
        try {
            String pdfUrl = pdfService.generateInvoicePdf(invoice, merchant);
            invoice.setPdfUrl(pdfUrl);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
        } catch (Exception e) {
            log.error("生成发票PDF失败: invoiceNo={}, error={}", invoice.getInvoiceNo(), e.getMessage(), e);
            // PDF生成失败不影响发票创建，后续可以重新生成
        }
        
        log.info("发票生成成功: merchantId={}, invoiceNo={}", merchantId, invoice.getInvoiceNo());
        
        // 发送发票邮件
        if (systemConfigService.isInvoiceAutoGenerate()) {
            sendInvoiceEmail(invoice, merchant);
        }
        
        return invoice;
    }

    /**
     * 生成发票编号
     */
    private String generateInvoiceNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "INV" + dateStr + randomStr;
    }

    /**
     * 发送发票邮件
     */
    private void sendInvoiceEmail(MerchantInvoice invoice, Merchant merchant) {
        if (merchant.getEmail() == null || merchant.getEmail().isEmpty()) {
            log.warn("商家邮箱为空，无法发送发票邮件: merchantId={}", merchant.getId());
            return;
        }
        
        try {
            String shopName = merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername();
            String downloadUrl = invoice.getPdfUrl();
            
            emailService.sendMerchantInvoiceEmail(
                    merchant.getEmail(),
                    shopName,
                    invoice.getInvoiceNo(),
                    invoice.getInvoiceDate().toString(),
                    invoice.getTotalAmount().toString(),  // 使用含税总金额
                    downloadUrl,
                    "zh"
            );
            
            // 更新发票状态为已发送
            invoice.setStatus(2);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
            
            log.info("发票邮件已发送: merchantId={}, invoiceNo={}, totalAmount={}", 
                    merchant.getId(), invoice.getInvoiceNo(), invoice.getTotalAmount());
        } catch (Exception e) {
            log.error("发送发票邮件失败: merchantId={}, error={}", merchant.getId(), e.getMessage(), e);
        }
    }

    /**
     * 获取商家发票列表
     */
    public List<MerchantInvoice> getMerchantInvoices(Long merchantId) {
        return invoiceMapper.selectList(
                new LambdaQueryWrapper<MerchantInvoice>()
                        .eq(MerchantInvoice::getMerchantId, merchantId)
                        .orderByDesc(MerchantInvoice::getCreatedAt)
        );
    }

    /**
     * 获取发票详情
     */
    public MerchantInvoice getInvoiceDetail(Long invoiceId) {
        MerchantInvoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "发票不存在");
        }
        return invoice;
    }

    /**
     * 重新发送发票邮件
     */
    @Transactional
    public void resendInvoiceEmail(Long invoiceId) {
        MerchantInvoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "发票不存在");
        }
        
        Merchant merchant = merchantMapper.selectById(invoice.getMerchantId());
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        sendInvoiceEmail(invoice, merchant);
    }
}
