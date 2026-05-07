package com.jm.vote.controller;

import com.jm.vote.dto.UserLoginRequest;
import com.jm.vote.dto.UserLoginResponse;
import com.jm.vote.dto.UserRegisterRequest;
import com.jm.vote.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        userAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse resp = userAuthService.login(request);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason() != null ? e.getReason() : "登录失败"));
        }
    }
}


