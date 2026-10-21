package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.CustomerTenantAccessInterceptor;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                InventoryAdminAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class InventoryAdminAuthorizationMockMvcTest {

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
    void adminRoleAloneShouldNotGrantInventoryAccess()
            throws Exception {

        mockMvc.perform(
                get("/admin/inventory")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void inventoryViewShouldAllowAllInventoryReads()
            throws Exception {

        mockMvc.perform(
                get("/admin/inventory")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/inventory/alerts")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/productos/1/inventory")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/inventory/movements")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void inventoryViewShouldNotAllowMutations()
            throws Exception {

        mockMvc.perform(
                post("/admin/productos/1/inventory/adjust")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                post("/admin/inventory/alerts/1/acknowledge")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void inventoryEditShouldAllowInventoryMutations()
            throws Exception {

        mockMvc.perform(
                post("/admin/productos/1/inventory/adjust")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/admin/inventory/alerts/1/acknowledge")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void inventoryEditAloneShouldNotGrantReads()
            throws Exception {

        mockMvc.perform(
                get("/admin/inventory")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void storeStaffInventoryPermissionsShouldAllowOperationalWork()
            throws Exception {

        mockMvc.perform(
                get("/admin/inventory")
                        .with(
                                staffWith(
                                        AdminPermission.INVENTORY_VIEW,
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/admin/productos/1/inventory/adjust")
                        .with(
                                staffWith(
                                        AdminPermission.INVENTORY_VIEW,
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void unknownInventoryOperationsShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                post("/admin/inventory/rebuild")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_VIEW,
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    private RequestPostProcessor adminWith(
            AdminPermission... permissions
    ) {
        return userWithRole(
                "STORE_ADMIN",
                "admin@stride.test",
                permissions
        );
    }

    private RequestPostProcessor staffWith(
            AdminPermission... permissions
    ) {
        return userWithRole(
                "STORE_STAFF",
                "staff@stride.test",
                permissions
        );
    }

    private RequestPostProcessor userWithRole(
            String role,
            String username,
            AdminPermission... permissions
    ) {

        SimpleGrantedAuthority[] authorities =
                Stream.concat(
                        Stream.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        ),
                        Arrays.stream(permissions)
                                .map(permission ->
                                        new SimpleGrantedAuthority(
                                                permission.authority()
                                        )
                                )
                )
                .toArray(
                        SimpleGrantedAuthority[]::new
                );

        return user(username)
                .authorities(authorities);
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/inventory")
        String dashboard() {
            return "OK";
        }

        @GetMapping("/admin/inventory/alerts")
        String alerts() {
            return "OK";
        }

        @PostMapping("/admin/inventory/alerts/{id}/acknowledge")
        String acknowledgeAlert() {
            return "OK";
        }

        @GetMapping("/admin/productos/{id}/inventory")
        String productInventory() {
            return "OK";
        }

        @PostMapping("/admin/productos/{id}/inventory/adjust")
        String adjustInventory() {
            return "OK";
        }

        @GetMapping("/admin/inventory/movements")
        String movements() {
            return "OK";
        }

        @PostMapping("/admin/inventory/rebuild")
        String rebuildInventory() {
            return "OK";
        }
    }
}
