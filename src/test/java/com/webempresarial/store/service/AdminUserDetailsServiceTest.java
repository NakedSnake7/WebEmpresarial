package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminUserDetailsServiceTest {

    private AdminUserRepository adminUserRepository;
    private AdminPermissionService adminPermissionService;
    private AdminUserDetailsService service;

    @BeforeEach
    void setUp() {

        adminUserRepository =
                mock(AdminUserRepository.class);

        adminPermissionService =
                new AdminPermissionService();

        service = new AdminUserDetailsService(
                adminUserRepository,
                adminPermissionService
        );
    }

    @Test
    void ownerReceivesRoleAndGranularPermissions() {

        AdminUser user = adminUser(
                "owner@test.com",
                AdminRole.STORE_OWNER,
                true
        );

        when(
                adminUserRepository.findByEmail(
                        "owner@test.com"
                )
        ).thenReturn(Optional.of(user));

        UserDetails details =
                service.loadUserByUsername(
                        " OWNER@TEST.COM "
                );

        assertThat(details.getAuthorities())
                .extracting(
                        authority ->
                                authority.getAuthority()
                )
                .contains(
                        "ROLE_STORE_OWNER",
                        AdminPermission.PRODUCT_EDIT.authority(),
                        AdminPermission.CRM_VIEW.authority(),
                        AdminPermission.SUBSCRIPTION_MANAGE.authority(),
                        AdminPermission.STRIPE_CONNECT_MANAGE.authority()
                );
    }

    @Test
    void storeAdminDoesNotReceiveOwnerOnlyPermissions() {

        AdminUser user = adminUser(
                "admin@test.com",
                AdminRole.STORE_ADMIN,
                true
        );

        when(
                adminUserRepository.findByEmail(
                        "admin@test.com"
                )
        ).thenReturn(Optional.of(user));

        UserDetails details =
                service.loadUserByUsername(
                        "admin@test.com"
                );

        assertThat(details.getAuthorities())
                .extracting(
                        authority ->
                                authority.getAuthority()
                )
                .contains(
                        "ROLE_STORE_ADMIN",
                        AdminPermission.PRODUCT_EDIT.authority(),
                        AdminPermission.SUBSCRIPTION_VIEW.authority()
                )
                .doesNotContain(
                        AdminPermission.SUBSCRIPTION_MANAGE.authority(),
                        AdminPermission.STRIPE_CONNECT_MANAGE.authority(),
                        AdminPermission.ROLE_MANAGE.authority()
                );
    }

    @Test
    void salesReceivesCommercialButNotOperationalWritePermissions() {

        AdminUser user = adminUser(
                "sales@test.com",
                AdminRole.SALES,
                true
        );

        when(
                adminUserRepository.findByEmail(
                        "sales@test.com"
                )
        ).thenReturn(Optional.of(user));

        UserDetails details =
                service.loadUserByUsername(
                        "sales@test.com"
                );

        assertThat(details.getAuthorities())
                .extracting(
                        authority ->
                                authority.getAuthority()
                )
                .contains(
                        "ROLE_SALES",
                        AdminPermission.CRM_EDIT.authority(),
                        AdminPermission.PIPELINE_MANAGE.authority(),
                        AdminPermission.PROPOSAL_CREATE.authority()
                )
                .doesNotContain(
                        AdminPermission.PRODUCT_EDIT.authority(),
                        AdminPermission.ORDER_UPDATE.authority(),
                        AdminPermission.SUBSCRIPTION_VIEW.authority()
                );
    }

    @Test
    void disabledAdminRemainsDisabled() {

        AdminUser user = adminUser(
                "disabled@test.com",
                AdminRole.STORE_STAFF,
                false
        );

        when(
                adminUserRepository.findByEmail(
                        "disabled@test.com"
                )
        ).thenReturn(Optional.of(user));

        UserDetails details =
                service.loadUserByUsername(
                        "disabled@test.com"
                );

        assertThat(
                details.isEnabled()
        ).isFalse();
    }

    private AdminUser adminUser(
            String email,
            AdminRole role,
            boolean enabled
    ) {

        AdminUser user = new AdminUser();

        user.setEmail(email);
        user.setPassword("{noop}password");
        user.setRole(role);
        user.setEnabled(enabled);

        return user;
    }
}
