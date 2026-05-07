package com.jm.vote.config;

import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.UserMapper;
import com.jm.vote.service.AdminDetailsService;
import com.jm.vote.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminDetailsService adminDetailsService;
    private final JwtUtil jwtUtil;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, adminDetailsService, merchantMapper, userMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // 启用 CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 允许所有 OPTIONS 预检请求通过
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 公开的认证接口（放在最前面）
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/common/upload").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // 商家支付相关接口（公开，因为未缴费商家需要访问）
                        .requestMatchers("/api/merchant/payment/**").permitAll()
                        // WebSocket 端点
                        .requestMatchers("/ws/**").permitAll()
                        // 测试接口（开发环境使用）
                        .requestMatchers("/api/test/**").permitAll()
                        // 需要认证的接口
                        .requestMatchers("/api/merchant/**").hasRole("MERCHANT")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider());
                // 移除 httpBasic，避免浏览器弹出认证框

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


