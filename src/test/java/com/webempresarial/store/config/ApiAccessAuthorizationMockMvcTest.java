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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                ApiAccessAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class ApiAccessAuthorizationMockMvcTest {

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
    void apiAccessShouldRemainClosedEvenForIntegrationPermissionHolders()
            throws Exception {

        for (String role : List.of(
                "STORE_OWNER",
                "STORE_ADMIN"
        )) {

            mockMvc.perform(
                    get("/admin/api")
                            .with(
                                    user(role + "@test")
                                            .roles(role)
                                            .authorities(
                                                    new SimpleGrantedAuthority(
                                                            AdminPermission
                                                                    .INTEGRATION_VIEW
                                                                    .authority()
                                                    ),
                                                    new SimpleGrantedAuthority(
                                                            AdminPermission
                                                                    .INTEGRATION_MANAGE
                                                                    .authority()
                                                    )
                                            )
                            )
            )
            .andExpect(status().isForbidden());

            mockMvc.perform(
                    get("/admin/api/keys")
                            .with(
                                    user(role + "@test")
                                            .roles(role)
                                            .authorities(
                                                    new SimpleGrantedAuthority(
                                                            AdminPermission
                                                                    .INTEGRATION_VIEW
                                                                    .authority()
                                                    ),
                                                    new SimpleGrantedAuthority(
                                                            AdminPermission
                                                                    .INTEGRATION_MANAGE
                                                                    .authority()
                                                    )
                                            )
                            )
            )
            .andExpect(status().isForbidden());
        }
    }

    @Test
    void apiAccessShouldRemainClosedForOtherAdministrativeRoles()
            throws Exception {

        for (String role : List.of(
                "SUPER_ADMIN",
                "STORE_STAFF",
                "SALES"
        )) {

            mockMvc.perform(
                    get("/admin/api")
                            .with(user(role + "@test").roles(role))
            )
            .andExpect(status().isForbidden());

            mockMvc.perform(
                    get("/admin/api/keys")
                            .with(user(role + "@test").roles(role))
            )
            .andExpect(status().isForbidden());
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/api")
        String root() {
            return "api";
        }

        @GetMapping("/admin/api/keys")
        String keys() {
            return "keys";
        }
    }
}
