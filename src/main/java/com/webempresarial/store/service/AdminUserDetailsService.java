package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final AdminPermissionService adminPermissionService;

    public AdminUserDetailsService(
            AdminUserRepository adminUserRepository,
            AdminPermissionService adminPermissionService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.adminPermissionService = adminPermissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail = email.trim().toLowerCase();

        AdminUser adminUser = adminUserRepository.findByEmail(
                normalizedEmail
        ).orElseThrow(() ->
                new UsernameNotFoundException(
                        "Admin no encontrado: " + email
                )
        );

        List<GrantedAuthority> authorities =
                buildAuthorities(adminUser);

        return User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPassword())
                .disabled(!adminUser.isEnabled())
                .authorities(authorities)
                .build();
    }

    private List<GrantedAuthority> buildAuthorities(
            AdminUser adminUser
    ) {

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        /*
         * Conservamos ROLE_* porque SecurityConfig y varias pruebas
         * existentes todavía dependen de los roles.
         */
        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + adminUser.getRole().name()
                )
        );

        /*
         * Añadimos los permisos granulares del rol.
         *
         * Ejemplo:
         * PRODUCT_EDIT -> PERM_PRODUCT_EDIT
         */
        for (AdminPermission permission :
                adminPermissionService.permissionsFor(
                        adminUser.getRole()
                )) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            permission.authority()
                    )
            );
        }

        return List.copyOf(authorities);
    }
}
