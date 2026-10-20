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
                CouponsAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class CouponsAuthorizationMockMvcTest {

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
    void storeAdminShouldNotAccessUnimplementedCouponsModule()
            throws Exception {

        mockMvc.perform(
                get("/admin/coupons")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/admin/coupons/10")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/coupons")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void superAdminShouldNotBypassCouponsFailClosed()
            throws Exception {

        mockMvc.perform(
                get("/admin/coupons")
                        .with(
                                user("root@webempresarial.com")
                                        .roles("SUPER_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/coupons")
        String list() {
            return "OK";
        }

        @GetMapping("/admin/coupons/{id}")
        String detail() {
            return "OK";
        }

        @PostMapping("/admin/coupons")
        String create() {
            return "OK";
        }
    }
}
