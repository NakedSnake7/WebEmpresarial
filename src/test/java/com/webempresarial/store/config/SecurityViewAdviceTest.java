package com.webempresarial.store.config;

import com.webempresarial.store.model.AdminPermission;

import org.junit.jupiter.api.Test;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityViewAdviceTest {

    private final SecurityViewAdvice advice =
            new SecurityViewAdvice();

    @Test
    void ownerShouldBeDetectedAndSeeBilling() {

        Authentication auth = authentication(
                "ROLE_STORE_OWNER",
                AdminPermission
                        .SUBSCRIPTION_VIEW
                        .authority(),
                AdminPermission
                        .SUBSCRIPTION_MANAGE
                        .authority(),
                AdminPermission
                        .STORE_EDIT
                        .authority(),
                AdminPermission
                        .STRIPE_CONNECT_MANAGE
                        .authority()
        );

        assertThat(
                advice.isStoreOwner(auth)
        ).isTrue();

        assertThat(
                advice.canViewBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageStoreSettings(auth)
        ).isTrue();

        assertThat(
                advice.canManageStripeConnect(auth)
        ).isTrue();
    }

    @Test
    void storeAdminShouldViewBillingWithoutManagingSubscription() {

        Authentication auth = authentication(
                "ROLE_STORE_ADMIN",
                AdminPermission
                        .SUBSCRIPTION_VIEW
                        .authority()
        );

        assertThat(
                advice.canViewBilling(auth)
        ).isTrue();

        assertThat(
                advice.canManageBilling(auth)
        ).isFalse();
    }

    @Test
    void analyticsPermissionShouldControlReportsVisibility() {

        Authentication allowed = authentication(
                "ROLE_STORE_ADMIN",
                AdminPermission.ANALYTICS_VIEW.authority()
        );

        Authentication denied = authentication(
                "ROLE_STORE_ADMIN"
        );

        assertThat(
                advice.canViewAnalytics(allowed)
        ).isTrue();

        assertThat(
                advice.canViewAnalytics(denied)
        ).isFalse();
    }

    @Test
    void salesShouldBeDetectedWithoutBillingOrSettingsAccess() {

        Authentication auth = authentication(
                "ROLE_SALES",
                AdminPermission.CRM_VIEW.authority()
        );

        assertThat(
                advice.isSales(auth)
        ).isTrue();

        assertThat(
                advice.canViewBilling(auth)
        ).isFalse();

        assertThat(
                advice.canManageBilling(auth)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(auth)
        ).isFalse();

        assertThat(
                advice.canManageStripeConnect(auth)
        ).isFalse();
    }

    @Test
    void missingAuthenticationShouldNotGrantAnything() {

        assertThat(
                advice.canViewBilling(null)
        ).isFalse();

        assertThat(
                advice.canManageBilling(null)
        ).isFalse();

        assertThat(
                advice.canViewAnalytics(null)
        ).isFalse();

        assertThat(
                advice.canViewTeam(null)
        ).isFalse();

        assertThat(
                advice.canManageStoreSettings(null)
        ).isFalse();

        assertThat(
                advice.canManageStripeConnect(null)
        ).isFalse();
    }

    private Authentication authentication(
            String... authorities
    ) {

        return new UsernamePasswordAuthenticationToken(
                "test@webempresarial.test",
                "password",
                List.of(authorities)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}
