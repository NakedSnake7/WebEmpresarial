package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentAdminAuthorizationServiceTest {

    private final CurrentAdminAuthorizationService service =
            new CurrentAdminAuthorizationService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDetectCurrentAdminPermission() {

        authenticateWith(
                AdminPermission.PRODUCT_CREATE,
                AdminPermission.CATEGORY_VIEW
        );

        assertThat(
                service.hasPermission(
                        AdminPermission.PRODUCT_CREATE
                )
        )
        .isTrue();

        assertThat(
                service.hasPermission(
                        AdminPermission.CATEGORY_MANAGE
                )
        )
        .isFalse();
    }

    @Test
    void requirePermissionShouldRejectMissingPermission() {

        authenticateWith(
                AdminPermission.PRODUCT_CREATE
        );

        assertThatThrownBy(
                () ->
                        service.requirePermission(
                                AdminPermission.CATEGORY_MANAGE
                        )
        )
        .isInstanceOf(
                AccessDeniedException.class
        );
    }

    @Test
    void unauthenticatedContextShouldHaveNoPermission() {

        SecurityContextHolder.clearContext();

        assertThat(
                service.hasPermission(
                        AdminPermission.PRODUCT_VIEW
                )
        )
        .isFalse();
    }

    private void authenticateWith(
            AdminPermission... permissions
    ) {

        List<SimpleGrantedAuthority> authorities =
                java.util.Arrays
                        .stream(permissions)
                        .map(permission ->
                                new SimpleGrantedAuthority(
                                        permission.authority()
                                )
                        )
                        .toList();

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin@stride.test",
                                "N/A",
                                authorities
                        )
                );
    }
}
