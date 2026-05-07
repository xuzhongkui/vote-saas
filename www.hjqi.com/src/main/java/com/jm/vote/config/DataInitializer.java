package com.jm.vote.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jm.vote.entity.*;
import com.jm.vote.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 在应用启动时自动初始化基础数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final ContentMapper contentMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("开始初始化系统数据...");
        log.info("========================================");

        try {
            // 1. 初始化管理员账号
            initAdmin();

            // 2. 初始化系统配置
            initSystemConfig();

            // 3. 初始化测试商家（可选）
            initTestMerchant();

            // 4. 初始化测试用户（可选）
            initTestUser();

            // 5. 初始化内容数据
            initContent();

            log.info("========================================");
            log.info("系统数据初始化完成！");
            log.info("========================================");
            log.info("默认管理员账号: admin / admin123");
            log.info("测试商家账号: merchant / merchant123");
            log.info("测试用户账号: user / user123");
            log.info("========================================");

        } catch (Exception e) {
            log.error("数据初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化管理员账号
     */
    private void initAdmin() {
        Long count = adminMapper.selectCount(null);
        if (count == 0) {
            log.info("初始化管理员账号...");

            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@vote.com");
            admin.setPhone("13800138000");
            admin.setNickname("系统管理员");
            admin.setRole("SUPER_ADMIN");
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setDeleted(0);

            adminMapper.insert(admin);
            log.info("✓ 管理员账号创建成功: admin / admin123");
        } else {
            // 确保管理员密码是正确加密的，并且状态正常
            log.info("检查管理员账号状态...");
            Admin admin = adminMapper.selectOne(
                    new QueryWrapper<Admin>().eq("username", "admin").eq("deleted", 0)
            );
            if (admin != null) {
                // 重置密码为 admin123（BCrypt加密）
                String encodedPassword = passwordEncoder.encode("admin123");
                admin.setPassword(encodedPassword);
                admin.setStatus(1); // 确保状态为启用
                admin.setUpdatedAt(LocalDateTime.now());
                adminMapper.updateById(admin);
                log.info("✓ 管理员密码已重置: admin / admin123");
            } else {
                log.warn("未找到 admin 账号，请检查数据库");
            }
        }
    }

    /**
     * 初始化系统配置
     */
    private void initSystemConfig() {
        Long count = systemConfigMapper.selectCount(null);
        if (count == 0) {
            log.info("初始化系统配置...");

            // 平台基础配置
            createConfig("platform.name", "私域商城 SaaS 系统", "PLATFORM", "平台名称");
            createConfig("platform.version", "1.0.0", "PLATFORM", "系统版本");
            createConfig("platform.copyright", "© 2025 私域商城 SaaS. All rights reserved.", "PLATFORM", "版权信息");

            // 商家审核配置
            createConfig("merchant.auto_audit", "false", "MERCHANT", "商家自动审核（true/false）");
            createConfig("merchant.service_fee", "999.00", "MERCHANT", "商家年费（元）");

            // 邮件配置
            createConfig("email.smtp_host", "smtp.example.com", "EMAIL", "SMTP服务器地址");
            createConfig("email.smtp_port", "587", "EMAIL", "SMTP端口");
            createConfig("email.username", "noreply@vote.com", "EMAIL", "发件人邮箱");
            createConfig("email.from_name", "私域商城", "EMAIL", "发件人名称");

            // 支付配置
            createConfig("payment.enabled", "true", "PAYMENT", "是否启用��付功能");
            createConfig("payment.mock_mode", "true", "PAYMENT", "是否使用模拟支付");

            // SEO配置
            createConfig("seo.title", "私域商城 - 专业的SaaS商城系统", "SEO", "网站标题");
            createConfig("seo.keywords", "商城,SaaS,电商,多商户", "SEO", "网站关键词");
            createConfig("seo.description", "私域商城是一个专业的多商户SaaS商城系统", "SEO", "网站描述");

            log.info("✓ 系统配置初始化完成");
        } else {
            log.info("✓ 系统配置已存在，跳过初始化");
        }
    }

    /**
     * 初始化测试商家
     */
    private void initTestMerchant() {
        Long count = merchantMapper.selectCount(null);
        if (count == 0) {
            log.info("初始化测试商家...");

            Merchant merchant = new Merchant();
            merchant.setUsername("merchant");
            merchant.setPassword(passwordEncoder.encode("merchant123"));
            merchant.setEmail("merchant@test.com");
            merchant.setPhone("13900139000");
            merchant.setShopNameZh("测试商家店铺");
            merchant.setShopNameEn("Test Merchant Shop");
            merchant.setContactName("张三");
            merchant.setContactPhone("13900139000");
            merchant.setStatus(1); // 已审核通过
            merchant.setAuditRemark("测试商家，自动审核通过");
            merchant.setInviteCode("TEST001");
            merchant.setCreatedAt(LocalDateTime.now());
            merchant.setUpdatedAt(LocalDateTime.now());
            merchant.setDeleted(0);

            merchantMapper.insert(merchant);
            log.info("✓ 测试商家创建成功: merchant / merchant123");
        } else {
            log.info("✓ 测试商家已存在，跳过初始化");
        }
    }

    /**
     * 初始化测试用户
     */
    private void initTestUser() {
        Long count = userMapper.selectCount(null);
        if (count == 0) {
            log.info("初始化测试用户...");

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@test.com");
            user.setPhone("13800138001");
            user.setNickname("测试用户");
            user.setMerchantId(1L); // 绑定到第一个商家
            user.setStatus(1);
            user.setLanguage("zh");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setDeleted(0);

            userMapper.insert(user);
            log.info("✓ 测试用户创建成功: user / user123");
        } else {
            log.info("✓ 测试用户已存在，跳过初始化");
        }
    }

    /**
     * 初始化内容数据
     */
    private void initContent() {
        Long count = contentMapper.selectCount(null);
        if (count == 0) {
            log.info("初始化内容数据...");

            // 欢迎公告
            createContent(
                "ANNOUNCEMENT",
                "欢迎使用私域商城系统",
                "Welcome to Private Domain Mall System",
                "欢迎使用私域商城SaaS系统！本系统为商家提供完整的电商解决方案。",
                "Welcome to Private Domain Mall SaaS System! We provide complete e-commerce solutions for merchants.",
                1,
                1
            );

            // 平台介绍
            createContent(
                "NEWS",
                "关于私域商城",
                "About Private Domain Mall",
                "私域商城是一个专业的多商户SaaS商城系统，为商家提供完整的电商功能。",
                "Private Domain Mall is a professional multi-merchant SaaS mall system that provides complete e-commerce functions for merchants.",
                2,
                1
            );

            // SEO内容
            createContent(
                "SEO",
                "私域商城 - 首页",
                "Private Domain Mall - Home",
                "私域商城是一个专业的SaaS商城系统，支持多商户入驻，提供完整的电商功能。",
                "Private Domain Mall is a professional SaaS mall system that supports multi-merchant settlement and provides complete e-commerce functions.",
                0,
                1
            );

            log.info("✓ 内容数据初始化完成");
        } else {
            log.info("✓ 内容数据已存在，跳过初始化");
        }
    }

    /**
     * 创建系统配置
     */
    private void createConfig(String key, String value, String group, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigGroup(group);
        config.setDescription(description);
        config.setConfigType("STRING");
        config.setSort(0);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setDeleted(0);
        systemConfigMapper.insert(config);
    }

    /**
     * 创建内容
     */
    private void createContent(String type, String titleZh, String titleEn,
                               String contentZh, String contentEn, int sort, int status) {
        Content content = new Content();
        content.setContentType(type);
        content.setTitleZh(titleZh);
        content.setTitleEn(titleEn);
        content.setContentZh(contentZh);
        content.setContentEn(contentEn);
        content.setSort(sort);
        content.setStatus(status);
        content.setPublishAt(LocalDateTime.now());
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        content.setDeleted(0);
        contentMapper.insert(content);
    }
}

