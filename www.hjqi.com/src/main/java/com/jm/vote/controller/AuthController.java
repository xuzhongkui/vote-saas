package com.jm.vote.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.dto.AdminInfoDTO;
import com.jm.vote.dto.AdminLoginRequest;
import com.jm.vote.dto.AdminLoginResponse;
import com.jm.vote.entity.Admin;
import com.jm.vote.repository.AdminMapper;
import com.jm.vote.service.OperationLogService;
import com.jm.vote.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminMapper adminMapper;
    private final OperationLogService operationLogService;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户名或密码错误"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "认证失败"));
        }

        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, request.getUsername())
                .eq(Admin::getDeleted, 0);
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "账号已禁用或不存在"));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ADMIN");
        claims.put("role", admin.getRole());
        claims.put("adminId", admin.getId());

        String token = jwtUtil.generateToken(admin.getUsername(), claims);
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpirationMillis();

        AdminInfoDTO adminInfo = new AdminInfoDTO();
        adminInfo.setId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setNickname(admin.getNickname());
        adminInfo.setRole(admin.getRole());
        adminInfo.setAvatar(admin.getAvatar());
        adminInfo.setEmail(admin.getEmail());
        adminInfo.setPhone(admin.getPhone());

        AdminLoginResponse resp = new AdminLoginResponse();
        resp.setToken(token);
        resp.setExpireAt(expireAt);
        resp.setAdmin(adminInfo);

        // 记录登录日志
        operationLogService.logOperationWithUser(
                admin.getId(),
                "ADMIN",
                admin.getUsername(),
                "LOGIN",
                "SYSTEM",
                "管理员登录",
                httpRequest
        );

        return ResponseEntity.ok(resp);
    }
}


