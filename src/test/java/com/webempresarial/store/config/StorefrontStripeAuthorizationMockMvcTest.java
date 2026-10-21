package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
                StorefrontStripeAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class StorefrontStripeAuthorizationMockMvcTest {

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
    void stripeStorefrontPostAllowlistShouldBePublicAndCsrfIgnored()
            throws Exception {

        mockMvc.perform(
                post(
                        "/api/stripe/create-session/100"
                )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/stripe/webhook")
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/stripe/connect/webhook")
        )
        .andExpect(status().isOk());
    }

    @Test
    void unsupportedStripeMethodsShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                get(
                        "/api/stripe/create-session/100"
                )
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/api/stripe/webhook")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/api/stripe/connect/webhook")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unknownStripeRoutesShouldFailClosed()
            throws Exception {

        /*
         * GET proves authorization fail-closed without CSRF
         * participating in the result.
         */
        mockMvc.perform(
                get("/api/stripe/unknown")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(status().isForbidden());

        /*
         * Supply a valid CSRF token explicitly so this 403
         * must come from denyAll(), not from CSRF protection.
         */
        mockMvc.perform(
                post("/api/stripe/unknown")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @PostMapping(
                "/api/stripe/create-session/{orderId}"
        )
        String createSession(
                @PathVariable Long orderId
        ) {
            return "ok";
        }

        @GetMapping(
                "/api/stripe/create-session/{orderId}"
        )
        String unsupportedCreateSessionGet(
                @PathVariable Long orderId
        ) {
            return "unexpected";
        }

        @PostMapping("/api/stripe/webhook")
        String webhook() {
            return "ok";
        }

        @GetMapping("/api/stripe/webhook")
        String unsupportedWebhookGet() {
            return "unexpected";
        }

        @PostMapping("/api/stripe/connect/webhook")
        String connectWebhook() {
            return "ok";
        }

        @GetMapping("/api/stripe/connect/webhook")
        String unsupportedConnectWebhookGet() {
            return "unexpected";
        }

        @PostMapping("/api/stripe/unknown")
        String unknownPost() {
            return "unexpected";
        }

        @GetMapping("/api/stripe/unknown")
        String unknownGet() {
            return "unexpected";
        }
    }
}
