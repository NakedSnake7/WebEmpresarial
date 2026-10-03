package com.webempresarial.store.interceptor;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminTenantAccessInterceptor
        implements HandlerInterceptor {

    private final AdminUserRepository adminUserRepository;
    private final StoreContextService storeContextService;

    public AdminTenantAccessInterceptor(
            AdminUserRepository adminUserRepository,
            StoreContextService storeContextService
    ) {
        this.adminUserRepository = adminUserRepository;
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
         * Spring Security se encarga de impedir el acceso
         * no autenticado. Aquí solo hacemos aislamiento
         * de tenant.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return true;
        }

        boolean superAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_SUPER_ADMIN"
                                        )
                        );

        /*
         * SUPER_ADMIN tiene acceso cross-tenant
         * por diseño.
         */
        if (superAdmin) {
            return true;
        }

        AdminUser adminUser =
                adminUserRepository
                        .findByEmail(
                                authentication
                                        .getName()
                                        .trim()
                                        .toLowerCase()
                        )
                        .orElse(null);

        if (adminUser == null
                || !adminUser.isEnabled()) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        if (adminUser.getRole()
                == AdminRole.SUPER_ADMIN) {
            return true;
        }

        Store adminStore = adminUser.getStore();

        if (adminStore == null
                || adminStore.getId() == null) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        Store requestStore;

        try {
            requestStore =
                    storeContextService
                            .getCurrentStore(request);

        } catch (RuntimeException ex) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        if (requestStore == null
                || requestStore.getId() == null
                || !adminStore.getId()
                        .equals(requestStore.getId())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        return true;
    }
}