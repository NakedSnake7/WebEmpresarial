package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                StripeConnectAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class StripeConnectAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuthenticationSuccessHandler
            adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService
            adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService
            authUserDetailsService;

    @MockitoBean
    private StoreContextService
            storeContextService;

    @MockitoBean
    private SidebarRegistry
            sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService
            inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService
            storeSettingsService;

    @MockitoBean
    private AdminTenantAccessInterceptor
            adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor
            subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver
            storeThemeResolver;

    @MockitoBean
    private FeatureAccessService
            featureAccessService;

    @Test
    void roleAloneShouldNotGrantStripeConnect() throws Exception {

        mockMvc.perform(
                post("/api/admin/stripe/connect/onboarding")
                        .with(
                                user("owner@acme.test")
                                        .roles("STORE_OWNER")
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void managePermissionWithoutCsrfShouldBeForbidden()
            throws Exception {

        mockMvc.perform(
                post("/api/admin/stripe/connect/onboarding")
                        .with(stripeManager())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void managePermissionShouldGrantOnboarding() throws Exception {

        mockMvc.perform(
                post("/api/admin/stripe/connect/onboarding")
                        .with(stripeManager())
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void managePermissionShouldGrantCallbacks() throws Exception {

        mockMvc.perform(
                get("/admin/stripe/connect/return")
                        .with(stripeManager())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/admin/stripe/connect/refresh")
                        .with(stripeManager())
        )
        .andExpect(status().isOk());
    }

    @Test
    void unrelatedPermissionShouldNotGrantStripeConnect()
            throws Exception {

        mockMvc.perform(
                post("/api/admin/stripe/connect/onboarding")
                        .with(
                                adminWith(
                                        AdminPermission
                                                .PAYMENT_METHOD_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unsupportedStripeConnectOperationsShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/stripe/connect/onboarding")
                        .with(stripeManager())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/stripe/connect/return")
                        .with(stripeManager())
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    private RequestPostProcessor stripeManager() {
        return adminWith(
                AdminPermission.STRIPE_CONNECT_MANAGE
        );
    }

    private RequestPostProcessor adminWith(
            AdminPermission... permissions
    ) {

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_STORE_OWNER"
                )
        );

        for (AdminPermission permission : permissions) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            permission.authority()
                    )
            );
        }

        return user("owner@acme.test")
                .authorities(
                        authorities.toArray(
                                GrantedAuthority[]::new
                        )
                );
    }

    @RestController
    static class TestController {

        @PostMapping("/api/admin/stripe/connect/onboarding")
        String onboarding() {
            return "OK";
        }

        @GetMapping("/admin/stripe/connect/return")
        String connectReturn() {
            return "OK";
        }

        @GetMapping("/admin/stripe/connect/refresh")
        String connectRefresh() {
            return "OK";
        }

        /*
         * Métodos no soportados declarados únicamente para probar
         * que SecurityConfig los rechaza mediante denyAll().
         */
        @GetMapping("/api/admin/stripe/connect/onboarding")
        String unsupportedOnboardingGet() {
            return "OK";
        }

        @PostMapping("/admin/stripe/connect/return")
        String unsupportedReturnPost() {
            return "OK";
        }
    }
}
