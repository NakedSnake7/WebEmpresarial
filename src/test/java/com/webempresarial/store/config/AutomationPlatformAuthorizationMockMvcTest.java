package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                AutomationPlatformAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class AutomationPlatformAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerTenantAccessInterceptor customerTenantAccessInterceptor;

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
    void superAdminShouldAccessPlatformAutomationDiagnostics()
            throws Exception {

        mockMvc.perform(
                get("/admin/automations/kernel")
                        .with(user("root@test").roles("SUPER_ADMIN"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/admin/automations/history")
                        .with(user("root@test").roles("SUPER_ADMIN"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/admin/automations/history/42")
                        .with(user("root@test").roles("SUPER_ADMIN"))
        )
        .andExpect(status().isOk());
    }

    @Test
    void tenantAdministrativeRolesShouldNotAccessPlatformAutomationDiagnostics()
            throws Exception {

        for (String role : List.of(
                "STORE_OWNER",
                "STORE_ADMIN",
                "STORE_STAFF",
                "SALES"
        )) {

            mockMvc.perform(
                    get("/admin/automations/kernel")
                            .with(user(role + "@test").roles(role))
            )
            .andExpect(status().isForbidden());

            mockMvc.perform(
                    get("/admin/automations/history")
                            .with(user(role + "@test").roles(role))
            )
            .andExpect(status().isForbidden());

            mockMvc.perform(
                    get("/admin/automations/history/42")
                            .with(user(role + "@test").roles(role))
            )
            .andExpect(status().isForbidden());
        }
    }

    @Test
    void tenantFacingAutomationRootShouldRemainClosed()
            throws Exception {

        mockMvc.perform(
                get("/admin/automations")
                        .with(user("root@test").roles("SUPER_ADMIN"))
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/admin/automations/future")
                        .with(user("root@test").roles("SUPER_ADMIN"))
        )
        .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/automations/kernel")
        String kernel() {
            return "OK";
        }

        @GetMapping("/admin/automations/history")
        String history() {
            return "OK";
        }

        @GetMapping("/admin/automations/history/{id}")
        String historyDetail(
                @PathVariable Long id
        ) {
            return "OK";
        }

        @GetMapping("/admin/automations")
        String automations() {
            return "OK";
        }

        @GetMapping("/admin/automations/future")
        String futureAutomation() {
            return "OK";
        }
    }
}
