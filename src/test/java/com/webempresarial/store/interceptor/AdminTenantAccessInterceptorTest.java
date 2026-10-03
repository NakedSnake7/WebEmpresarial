package com.webempresarial.store.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.StoreContextService;

@ExtendWith(MockitoExtension.class)
class AdminTenantAccessInterceptorTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AdminTenantAccessInterceptor interceptor;

    @BeforeEach
    void setUp() {

        interceptor =
                new AdminTenantAccessInterceptor(
                        adminUserRepository,
                        storeContextService
                );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowStoreAdminForOwnTenant()
            throws Exception {

        Store stride = store(3L);

        AdminUser admin =
                admin(
                        "admin@stride.test",
                        AdminRole.STORE_ADMIN,
                        stride,
                        true
                );

        authenticate(
                "admin@stride.test",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(Optional.of(admin));

        when(storeContextService.getCurrentStore(request))
                .thenReturn(stride);

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isTrue();

        verify(response, never())
                .sendError(anyInt());
    }

    @Test
    void shouldRejectStoreAdminForDifferentTenant()
            throws Exception {

        Store stride = store(3L);
        Store barleyPunch = store(4L);

        AdminUser admin =
                admin(
                        "admin@stride.test",
                        AdminRole.STORE_ADMIN,
                        stride,
                        true
                );

        authenticate(
                "admin@stride.test",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(Optional.of(admin));

        when(storeContextService.getCurrentStore(request))
                .thenReturn(barleyPunch);

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    @Test
    void shouldAllowSuperAdminAcrossTenants()
            throws Exception {

        authenticate(
                "superadmin@webempresarial.test",
                "ROLE_SUPER_ADMIN"
        );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isTrue();

        verifyNoInteractions(adminUserRepository);
        verifyNoInteractions(storeContextService);

        verify(response, never())
                .sendError(anyInt());
    }

    @Test
    void shouldRejectDisabledAdmin()
            throws Exception {

        AdminUser admin =
                mock(AdminUser.class);

        authenticate(
                "admin@stride.test",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(Optional.of(admin));

        when(admin.isEnabled())
                .thenReturn(false);

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed)
                .isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verify(adminUserRepository)
                .findByEmail(
                        "admin@stride.test"
                );

        verify(admin)
                .isEnabled();

        /*
         * Fail fast:
         * un administrador deshabilitado no debe llegar
         * siquiera a resolución/autorización de tenant.
         */
        verifyNoInteractions(storeContextService);

        verify(admin, never())
                .getRole();

        verify(admin, never())
                .getStore();
    }

    @Test
    void shouldRejectAdminWithoutStore()
            throws Exception {

        AdminUser admin =
                admin(
                        "admin@test.com",
                        AdminRole.STORE_ADMIN,
                        null,
                        true
                );

        authenticate(
                "admin@test.com",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "admin@test.com"
        )).thenReturn(Optional.of(admin));

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectWhenAuthenticatedAdminDoesNotExist()
            throws Exception {

        authenticate(
                "ghost@test.com",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "ghost@test.com"
        )).thenReturn(Optional.empty());

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );

        verifyNoInteractions(storeContextService);
    }

    @Test
    void shouldRejectWhenRequestTenantCannotBeResolved()
            throws Exception {

        Store stride = store(3L);

        AdminUser admin =
                admin(
                        "admin@stride.test",
                        AdminRole.STORE_ADMIN,
                        stride,
                        true
                );

        authenticate(
                "admin@stride.test",
                "ROLE_STORE_ADMIN"
        );

        when(adminUserRepository.findByEmail(
                "admin@stride.test"
        )).thenReturn(Optional.of(admin));

        when(storeContextService.getCurrentStore(request))
                .thenThrow(
                        new RuntimeException(
                                "Tenant no encontrado"
                        )
                );

        boolean allowed =
                interceptor.preHandle(
                        request,
                        response,
                        new Object()
                );

        assertThat(allowed).isFalse();

        verify(response).sendError(
                HttpServletResponse.SC_FORBIDDEN
        );
    }

    private void authenticate(
            String username,
            String authority
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        java.util.List.of(
                                new SimpleGrantedAuthority(
                                        authority
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    private Store store(Long id) {

        Store store = mock(Store.class);

        when(store.getId())
                .thenReturn(id);

        return store;
    }

    private AdminUser admin(
            String email,
            AdminRole role,
            Store store,
            boolean enabled
    ) {

        AdminUser admin =
                mock(AdminUser.class);

        when(admin.getRole())
                .thenReturn(role);

        when(admin.isEnabled())
                .thenReturn(enabled);

        if (store != null) {
            when(admin.getStore())
                    .thenReturn(store);
        }

        return admin;
    }
}