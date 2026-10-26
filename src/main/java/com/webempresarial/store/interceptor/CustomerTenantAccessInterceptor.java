package com.webempresarial.store.interceptor;

import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AuthUserRepository;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CustomerTenantAccessInterceptor
        implements HandlerInterceptor {

    private final AuthUserRepository authUserRepository;
    private final StoreContextService storeContextService;

    public CustomerTenantAccessInterceptor(
            AuthUserRepository authUserRepository,
            StoreContextService storeContextService
    ) {
        this.authUserRepository = authUserRepository;
        this.storeContextService = storeContextService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /*
         * Spring Security decide si una ruta requiere login.
         * Aquí sólo aplicamos aislamiento tenant a ROLE_CLIENTE.
         */
        if (
                authentication == null
                || !authentication.isAuthenticated()
        ) {
            return true;
        }

        boolean customer =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        "ROLE_CLIENTE"
                                                .equals(
                                                        authority
                                                                .getAuthority()
                                                )
                        );

        /*
         * Este interceptor no gobierna identidades admin.
         */
        if (!customer) {
            return true;
        }

        String email =
                authentication.getName();

        if (email == null || email.isBlank()) {
            return forbidden(response);
        }

        Store requestStore;

        try {
            requestStore =
                    storeContextService
                            .getCurrentStore(request);

        } catch (RuntimeException ex) {
            return forbidden(response);
        }

        if (
                requestStore == null
                || requestStore.getId() == null
        ) {
            return forbidden(response);
        }

        AuthUser authUser =
                authUserRepository
                        .findByEmailAndCliente_Store(
                                email
                                        .trim()
                                        .toLowerCase(),
                                requestStore
                        )
                        .orElse(null);

        if (
                authUser == null
                || !authUser.isEnabled()
        ) {
            return forbidden(response);
        }

        Cliente cliente =
                authUser.getCliente();

        if (cliente == null) {
            return forbidden(response);
        }

        Store credentialStore =
                cliente.getStore();

        if (
                credentialStore == null
                || credentialStore.getId() == null
                || !credentialStore
                        .getId()
                        .equals(
                                requestStore.getId()
                        )
        ) {
            return forbidden(response);
        }

        return true;
    }

    private boolean forbidden(
            HttpServletResponse response
    ) throws Exception {

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        return false;
    }
}
