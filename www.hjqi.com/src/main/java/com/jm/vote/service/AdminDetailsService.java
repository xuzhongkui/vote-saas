package com.jm.vote.service;

import com.jm.vote.entity.Admin;
import com.jm.vote.repository.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .eq(Admin::getDeleted, 0)
        );
        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found");
        }
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole()));

        boolean enabled = admin.getStatus() != null && admin.getStatus() == 1;

        return new User(
                admin.getUsername(),
                admin.getPassword(),
                enabled,
                true,
                true,
                true,
                authorities
        );
    }
}


