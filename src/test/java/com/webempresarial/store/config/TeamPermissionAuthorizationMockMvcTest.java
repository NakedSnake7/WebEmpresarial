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
                TeamPermissionAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class TeamPermissionAuthorizationMockMvcTest {

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
    void teamViewShouldAllowUserList()
            throws Exception {

        mockMvc.perform(
                get("/admin/users")
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                role("STORE_ADMIN"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_VIEW
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void teamViewAloneShouldNotAllowInviteForm()
            throws Exception {

        mockMvc.perform(
                get("/admin/users/nuevo")
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                role("STORE_ADMIN"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_VIEW
                                                )
                                        )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void teamInviteShouldAllowInviteForm()
            throws Exception {

        mockMvc.perform(
                get("/admin/users/nuevo")
                        .with(
                                user("owner@stride.test")
                                        .authorities(
                                                role("STORE_OWNER"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_INVITE
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void teamInviteShouldAllowSavingUser()
            throws Exception {

        mockMvc.perform(
                post("/admin/users/guardar")
                        .with(csrf())
                        .with(
                                user("owner@stride.test")
                                        .authorities(
                                                role("STORE_OWNER"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_INVITE
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void teamEditShouldAllowStateChange()
            throws Exception {

        mockMvc.perform(
                post("/admin/users/estado/42")
                        .with(csrf())
                        .with(
                                user("owner@stride.test")
                                        .authorities(
                                                role("STORE_OWNER"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_EDIT
                                                )
                                        )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void teamViewAloneShouldNotAllowStateChange()
            throws Exception {

        mockMvc.perform(
                post("/admin/users/estado/42")
                        .with(csrf())
                        .with(
                                user("admin@stride.test")
                                        .authorities(
                                                role("STORE_ADMIN"),
                                                permission(
                                                        AdminPermission
                                                                .TEAM_VIEW
                                                )
                                        )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void staffShouldNotAccessTeam()
            throws Exception {

        mockMvc.perform(
                get("/admin/users")
                        .with(
                                user("staff@stride.test")
                                        .roles("STORE_STAFF")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void roleAloneShouldNotGrantTeamAccess()
            throws Exception {

        mockMvc.perform(
                get("/admin/users")
                        .with(
                                user("owner@stride.test")
                                        .roles("STORE_OWNER")
                        )
        )
        .andExpect(status().isForbidden());
    }

    private SimpleGrantedAuthority role(
            String role
    ) {
        return new SimpleGrantedAuthority(
                "ROLE_" + role
        );
    }

    private SimpleGrantedAuthority permission(
            AdminPermission permission
    ) {
        return new SimpleGrantedAuthority(
                permission.authority()
        );
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/users")
        String users() {
            return "OK";
        }

        @GetMapping("/admin/users/nuevo")
        String newUser() {
            return "OK";
        }

        @PostMapping("/admin/users/guardar")
        String save() {
            return "OK";
        }

        @PostMapping("/admin/users/estado/{id}")
        String status() {
            return "OK";
        }
    }
}
