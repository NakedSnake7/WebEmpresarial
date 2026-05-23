package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        AdminUser adminUser = adminUserRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new UsernameNotFoundException("Admin no encontrado: " + email)
        );

        return User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPassword())
                .disabled(!adminUser.isEnabled())
                .roles(adminUser.getRole().name())
                .build();
    }
}