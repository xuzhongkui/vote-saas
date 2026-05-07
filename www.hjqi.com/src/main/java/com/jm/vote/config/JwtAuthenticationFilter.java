package com.jm.vote.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.User;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.UserMapper;
import com.jm.vote.service.AdminDetailsService;
import com.jm.vote.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminDetailsService adminDetailsService;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        // 跳过公开路径，不需要JWT验证
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        log.info("JWT Filter - URI: {}, Auth Header: {}", requestURI, 
                authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("JWT Filter - No Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;
        String type;
        try {
            Claims claims = jwtUtil.parseToken(token);
            username = claims.getSubject();
            type = claims.get("type", String.class);
            log.info("JWT Filter - Username: {}, Type: {}", username, type);
            
            if (username == null || jwtUtil.isTokenExpired(token)) {
                log.info("JWT Filter - Token expired or username is null");
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            log.error("JWT Filter - Token parse error: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if ("ADMIN".equals(type)) {
                try {
                    UserDetails userDetails = adminDetailsService.loadUserByUsername(username);
                    log.info("JWT Filter - Admin authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("JWT Filter - Admin authentication set successfully");
                } catch (Exception e) {
                    log.error("JWT Filter - Admin authentication error: {}", e.getMessage());
                }
            } else if ("MERCHANT".equals(type)) {
                LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Merchant::getUsername, username)
                        .eq(Merchant::getDeleted, 0);
                Merchant merchant = merchantMapper.selectOne(queryWrapper);
                if (merchant != null && merchant.getStatus() != null && merchant.getStatus() == 1) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    merchant.getUsername(),
                                    null,
                                    java.util.List.of(() -> "ROLE_MERCHANT")
                            );
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("JWT Filter - Merchant authentication set successfully");
                }
            } else if ("USER".equals(type)) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username)
                        .eq(User::getDeleted, 0);
                User user = userMapper.selectOne(queryWrapper);
                if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null,
                                    java.util.List.of(() -> "ROLE_USER")
                            );
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("JWT Filter - User authentication set successfully");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * 判断是否为公开路径（不需要JWT验证）
     */
    private boolean isPublicPath(String requestURI) {
        // 公开的认证相关路径
        if (requestURI.startsWith("/api/auth/")) {
            // 登录、注册、密码找回等公开接口
            if (requestURI.contains("/login") || 
                requestURI.contains("/register") ||
                requestURI.contains("/password/forgot") ||
                requestURI.contains("/password/verify") ||
                requestURI.contains("/password/reset") ||
                requestURI.contains("/forgot-password") ||
                requestURI.contains("/reset-password")) {
                return true;
            }
        }
        // 公开内容接口
        if (requestURI.startsWith("/api/public/")) {
            return true;
        }
        // 文件上传接口
        if (requestURI.startsWith("/api/common/upload") || requestURI.startsWith("/upload/")) {
            return true;
        }
        return false;
    }
}


