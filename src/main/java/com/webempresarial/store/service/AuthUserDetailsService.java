package com.webempresarial.store.service;

import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AuthUserRepository;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails
        .UsernameNotFoundException;

import org.springframework.stereotype.Service;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuthUserDetailsService
        implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final StoreContextService storeContextService;

    public AuthUserDetailsService(
            AuthUserRepository authUserRepository,
            StoreContextService storeContextService
    ) {
        this.authUserRepository = authUserRepository;
        this.storeContextService = storeContextService;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        String normalizedEmail =
                normalizeEmail(email);

        HttpServletRequest request =
                currentRequest();

        Store store;

        try {
            store =
                    storeContextService
                            .getCurrentStore(request);

        } catch (RuntimeException ex) {

            throw new UsernameNotFoundException(
                    "No se pudo resolver el tenant "
                    + "para la autenticación",
                    ex
            );
        }

        if (store == null || store.getId() == null) {
            throw new UsernameNotFoundException(
                    "Tenant no válido para la autenticación"
            );
        }

        AuthUser authUser =
                authUserRepository
                        .findByEmailAndCliente_Store(
                                normalizedEmail,
                                store
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Usuario no encontrado"
                                        )
                        );

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_"
                        + authUser.getRole().name()
                );

        return org.springframework.security.core.userdetails
                .User
                .builder()
                .username(authUser.getEmail())
                .password(authUser.getPassword())
                .authorities(
                        Collections.singleton(
                                authority
                        )
                )
                .disabled(!authUser.isEnabled())
                .build();
    }

    private HttpServletRequest currentRequest() {

        if (
                !(RequestContextHolder
                        .getRequestAttributes()
                        instanceof ServletRequestAttributes attributes)
        ) {
            throw new UsernameNotFoundException(
                    "No existe contexto HTTP "
                    + "para autenticar al cliente"
            );
        }

        return attributes.getRequest();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException(
                    "Email requerido"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }
}
