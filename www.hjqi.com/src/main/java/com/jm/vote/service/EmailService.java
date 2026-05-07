package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.EmailTemplate;
import com.jm.vote.repository.EmailTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateMapper emailTemplateMapper;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String mailFrom;
    
    // 用于处理字符串模板的 TemplateEngine
    private TemplateEngine stringTemplateEngine;
    
    /**
     * 初始化字符串模板引擎
     */
    private TemplateEngine getStringTemplateEngine() {
        if (stringTemplateEngine == null) {
            StringTemplateResolver templateResolver = new StringTemplateResolver();
            templateResolver.setTemplateMode(TemplateMode.HTML);
            
            stringTemplateEngine = new TemplateEngine();
            stringTemplateEngine.setTemplateResolver(templateResolver);
        }
        return stringTemplateEngine;
    }

    /**
     * 发送商家欢迎邮件（异步）
     */
    @Async("emailExecutor")
    public void sendMerchantWelcomeEmail(String to, String username, String shopName, 
                                         String inviteCode, String promotionLink, 
                                         String merchantReward, String userRewardRate, String language) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "shopName", shopName,
                "inviteCode", inviteCode,
                "promotionLink", promotionLink,
                "merchantReward", merchantReward,
                "userRewardRate", userRewardRate
        );
        sendTemplateEmailInternal("MERCHANT_WELCOME", to, variables, language);
    }

    /**
     * 发送商家审核通过邮件（异步）
     */
    @Async("emailExecutor")
    public void sendMerchantAuditApprovedEmail(String to, String shopName, String fee, 
                                               String paymentLink, String language) {
        Map<String, Object> variables = Map.of(
                "shopName", shopName,
                "fee", fee,
                "paymentLink", paymentLink
        );
        sendTemplateEmailInternal("MERCHANT_AUDIT_APPROVED", to, variables, language);
    }

    /**
     * 发送商家审核拒绝邮件（异步）
     */
    @Async("emailExecutor")
    public void sendMerchantAuditRejectedEmail(String to, String shopName, String reason, String language) {
        Map<String, Object> variables = Map.of(
                "shopName", shopName,
                "reason", reason
        );
        sendTemplateEmailInternal("MERCHANT_AUDIT_REJECTED", to, variables, language);
    }

    /**
     * 发送商家发票邮件（异步）
     */
    @Async("emailExecutor")
    public void sendMerchantInvoiceEmail(String to, String shopName, String invoiceNo, 
                                        String invoiceDate, String amount, String downloadUrl, String language) {
        Map<String, Object> variables = Map.of(
                "shopName", shopName,
                "invoiceNo", invoiceNo,
                "invoiceDate", invoiceDate,
                "amount", amount,
                "downloadUrl", downloadUrl
        );
        sendTemplateEmailInternal("MERCHANT_INVOICE", to, variables, language);
    }

    /**
     * 发送欢迎邮件（异步）
     */
    @Async("emailExecutor")
    public void sendWelcomeEmail(String to, String username, String shopName, String language) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "shopName", shopName
        );
        sendTemplateEmailInternal("WELCOME", to, variables, language);
    }

    /**
     * 发送订单通知邮件（异步）
     */
    @Async("emailExecutor")
    public void sendOrderNotificationEmail(String to, String orderNo, String amount, String language) {
        Map<String, Object> variables = Map.of(
                "orderNo", orderNo,
                "amount", amount
        );
        sendTemplateEmailInternal("ORDER_NOTIFICATION", to, variables, language);
    }

    /**
     * 发送发票邮件（异步）
     */
    @Async("emailExecutor")
    public void sendInvoiceEmail(String to, String invoiceNo, String downloadUrl, String language) {
        Map<String, Object> variables = Map.of(
                "invoiceNo", invoiceNo,
                "downloadUrl", downloadUrl
        );
        sendTemplateEmailInternal("INVOICE", to, variables, language);
    }

    /**
     * 使用模板发送邮件（公开方法，异步）
     */
    @Async("emailExecutor")
    public void sendTemplateEmail(String templateKey, String to, Map<String, Object> variables, String language) {
        sendTemplateEmailInternal(templateKey, to, variables, language);
    }

    /**
     * 内部邮件发送方法（实际执行发送）
     */
    private void sendTemplateEmailInternal(String templateKey, String to, Map<String, Object> variables, String language) {
        try {
            EmailTemplate template = emailTemplateMapper.selectOne(
                    new LambdaQueryWrapper<EmailTemplate>()
                            .eq(EmailTemplate::getTemplateKey, templateKey)
                            .eq(EmailTemplate::getStatus, 1)
            );

            if (template == null) {
                log.warn("邮件模板不存在: {}", templateKey);
                return;
            }

            String subject = "zh".equals(language) ? template.getSubjectZh() : template.getSubjectEn();
            String content = "zh".equals(language) ? template.getContentZh() : template.getContentEn();

            // 使用字符串模板引擎渲染模板
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }

            // 使用 StringTemplateResolver 处理字符串模板
            String htmlContent = getStringTemplateEngine().process(content, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom); // 设置发件人地址
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", templateKey, to);
        } catch (Exception e) {
            log.error("发送邮件失败: {} -> {}, error: {}", templateKey, to, e.getMessage(), e);
            // 异步方法中不抛出异常，只记录日志
        }
    }
}

