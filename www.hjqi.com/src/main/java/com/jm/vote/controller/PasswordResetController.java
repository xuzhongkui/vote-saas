package com.jm.vote.controller;

import com.jm.vote.dto.ForgotPasswordRequest;
import com.jm.vote.dto.ResetPasswordRequest;
import com.jm.vote.dto.VerifyCodeRequest;
import com.jm.vote.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of("message", "验证码已发送到您的邮箱，请查收"));
    }

    @PostMapping("/password/verify")
    public ResponseEntity<Map<String, String>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        passwordResetService.verifyCode(request);
        return ResponseEntity.ok(Map.of("message", "验证码验证成功"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "密码重置成功"));
    }
}

