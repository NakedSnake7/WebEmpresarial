package com.webempresarial.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;

@WebMvcTest(
        controllers = SaaSCheckoutController.class
)
@Import(SecurityConfig.class)
class SaaSCheckoutSecurityMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;
    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private FeatureAccessService featureAccessService;
    @MockitoBean
    private SidebarRegistry sidebarRegistry;
    @MockitoBean
    private InventoryPersistentAlertService inventoryPersistentAlertService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;

    @BeforeEach
    void allowMvcInterceptors() throws Exception {

        when(
            adminTenantAccessInterceptor.preHandle(
                any(),
                any(),
                any()
            )
        ).thenReturn(true);

        when(
            subscriptionInterceptor.preHandle(
                any(),
                any(),
                any()
            )
        ).thenReturn(true);
    }

    @Test
    @WithAnonymousUser
    void shouldAllowAnonymousAccessToBillingSuccess()
            throws Exception {

        mockMvc.perform(
                get("/billing/success")
                        .with(anonymous())
        )
        .andExpect(status().isOk())
        .andExpect(view().name("billing/success"))
        .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                        "Pago recibido"
                )
        ));
    }

    @Test
    @WithAnonymousUser
    void shouldAllowAnonymousAccessWithStripeSessionId()
            throws Exception {

        mockMvc.perform(
                get("/billing/success")
                        .param(
                                "session_id",
                                "cs_test_123456"
                        )
                        .with(anonymous())
        )
        .andExpect(status().isOk())
        .andExpect(view().name("billing/success"))
        .andExpect(content().string(
                org.hamcrest.Matchers.containsString(
                        "Estamos preparando tu cuenta"
                )
        ));
    }
}
