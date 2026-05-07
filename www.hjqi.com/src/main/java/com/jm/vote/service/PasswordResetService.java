package com.jm.vote.service;

import com.jm.vote.dto.ForgotPasswordRequest;
import com.jm.vote.dto.ResetPasswordRequest;
import com.jm.vote.dto.VerifyCodeRequest;
import com.jm.vote.entity.*;
import com.jm.vote.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenMapper tokenMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String mailFrom;

    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail();
        String userType = request.getUserType();
        
        // 查找用户
        Object user = null;
        
        if ("USER".equals(userType)) {
            User u = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getEmail, email)
                            .eq(User::getDeleted, 0)
            );
            if (u != null) {
                user = u;
            }
        } else if ("MERCHANT".equals(userType)) {
            Merchant m = merchantMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                            .eq(Merchant::getEmail, email)
                            .eq(Merchant::getDeleted, 0)
            );
            if (m != null) {
                user = m;
            }
        } else if ("ADMIN".equals(userType)) {
            Admin a = adminMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                            .eq(Admin::getEmail, email)
                            .eq(Admin::getDeleted, 0)
            );
            if (a != null) {
                user = a;
            }
        }
        
        if (user == null) {
            log.warn("忘记密码请求失败：未找到邮箱 {} 对应的 {} 用户", email, userType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该邮箱未注册或用户类型不匹配");
        }
        
        // 生成6位随机验证码
        String verificationCode = generateVerificationCode();
        
        // 生成重置令牌（用于后续验证）
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10); // 10分钟有效期
        
        // 删除该邮箱之前的未使用验证码记录
        tokenMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PasswordResetToken>()
                        .eq(PasswordResetToken::getEmail, email)
                        .eq(PasswordResetToken::getUserType, userType)
                        .eq(PasswordResetToken::getUsed, 0)
        );
        
        // 保存验证码和令牌
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setCode(verificationCode);
        resetToken.setUserType(userType);
        resetToken.setUsed(0);
        resetToken.setExpireAt(expireAt);
        resetToken.setCreatedAt(LocalDateTime.now());
        tokenMapper.insert(resetToken);
        
        // 异步发送邮件
        sendPasswordResetEmailAsync(email, verificationCode);
        
        log.info("密码重置验证码已生成: email={}", email);
    }
    
    /**
     * 异步发送密码重置邮件
     */
    @Async("emailExecutor")
    public void sendPasswordResetEmailAsync(String email, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("密码重置验证码");
            message.setText("您正在重置密码，验证码为：" + verificationCode + "（10分钟内有效）\n\n" +
                    "如果这不是您的操作，请忽略此邮件。");
            mailSender.send(message);
            log.info("密码重置验证码邮件已发送: email={}", email);
        } catch (Exception e) {
            log.error("发送密码重置邮件失败: email={}, error={}", email, e.getMessage(), e);
            // 异步方法中不抛出异常，只记录日志
        }
    }
    
    public void verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String userType = request.getUserType();
        
        // 查找验证码记录
        PasswordResetToken resetToken = tokenMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PasswordResetToken>()
                        .eq(PasswordResetToken::getEmail, email)
                        .eq(PasswordResetToken::getCode, code)
                        .eq(PasswordResetToken::getUserType, userType)
                        .eq(PasswordResetToken::getUsed, 0)
        );
        
        if (resetToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码错误或已使用");
        }
        
        if (resetToken.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码已过期");
        }
        
        // 验证成功，不删除记录，等待重置密码时再标记为已使用
        log.info("验证码验证成功: email={}, code={}", email, code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        String userType = request.getUserType();
        
        // 查找验证码记录
        PasswordResetToken resetToken = tokenMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PasswordResetToken>()
                        .eq(PasswordResetToken::getEmail, email)
                        .eq(PasswordResetToken::getCode, code)
                        .eq(PasswordResetToken::getUserType, userType)
                        .eq(PasswordResetToken::getUsed, 0)
        );
        
        if (resetToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码错误或已使用");
        }
        
        if (resetToken.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码已过期");
        }
        
        // 更新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        
        if ("USER".equals(userType)) {
            User user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getEmail, email)
                            .eq(User::getDeleted, 0)
            );
            if (user != null) {
                user.setPassword(encryptedPassword);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
            }
        } else if ("MERCHANT".equals(userType)) {
            Merchant merchant = merchantMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                            .eq(Merchant::getEmail, email)
                            .eq(Merchant::getDeleted, 0)
            );
            if (merchant != null) {
                merchant.setPassword(encryptedPassword);
                merchant.setUpdatedAt(LocalDateTime.now());
                merchantMapper.updateById(merchant);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
            }
        } else if ("ADMIN".equals(userType)) {
            Admin admin = adminMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                            .eq(Admin::getEmail, email)
                            .eq(Admin::getDeleted, 0)
            );
            if (admin != null) {
                admin.setPassword(encryptedPassword);
                admin.setUpdatedAt(LocalDateTime.now());
                adminMapper.updateById(admin);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在");
            }
        }
        
        // 标记验证码为已使用
        resetToken.setUsed(1);
        tokenMapper.updateById(resetToken);
        
        log.info("密码重置成功: email={}, userType={}", email, userType);
    }
}

