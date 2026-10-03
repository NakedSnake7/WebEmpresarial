package com.webempresarial.store.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stripe.model.checkout.Session;

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
import com.webempresarial.store.service.StripeSaaSCheckoutService;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;

@WebMvcTest(
        controllers = BillingRestController.class
)
@Import(SecurityConfig.class)
class BillingRestControllerSecurityMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StripeSaaSCheckoutService stripeSaaSCheckoutService;

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
    private StoreThemeResolver storeThemeResolver;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

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
    void shouldAllowAnonymousSaaSCheckoutWithoutCsrfToken()
            throws Exception {

        Session session = new Session();

        session.setUrl(
            "https://checkout.stripe.com/c/pay/cs_test_webempresarial"
        );

        when(
            stripeSaaSCheckoutService
                .createSaaSCheckoutSession(any())
        ).thenReturn(session);

        mockMvc.perform(
                post("/api/billing/checkout")
                    .with(anonymous())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "companyName": "Acme Store",
                          "domain": "acme",
                          "ownerName": "John Doe",
                          "email": "john@acme.test",
                          "plan": "PRO"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.checkoutUrl")
                    .value(
                        "https://checkout.stripe.com/c/pay/cs_test_webempresarial"
                    )
            );
    }
}
