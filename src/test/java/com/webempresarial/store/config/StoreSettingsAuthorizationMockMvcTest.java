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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                StoreSettingsAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class StoreSettingsAuthorizationMockMvcTest {

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
    void roleAloneShouldNotGrantStoreSettingsAccess()
            throws Exception {

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void commercialInfoPermissionShouldGrantGeneralSettingsPost()
            throws Exception {

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .STORE_COMMERCIAL_INFO_EDIT
                                                                .authority()
                                                )
                                        )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void commercialInfoPermissionShouldNotGrantShippingPost()
            throws Exception {

        mockMvc.perform(
                post("/admin/store/settings/shipping")
                        .with(
                                user("admin@acme.test")
                                        .roles("STORE_ADMIN")
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        AdminPermission
                                                                .STORE_COMMERCIAL_INFO_EDIT
                                                                .authority()
                                                )
                                        )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeViewShouldOnlyGrantSettingsRead()
            throws Exception {

        RequestPostProcessor actor =
                adminWith(
                        AdminPermission.STORE_VIEW
                );

        mockMvc.perform(
                get("/admin/store/settings")
                        .with(actor)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/store/settings/shipping")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeEditShouldGrantSettingsAndShipping()
            throws Exception {

        RequestPostProcessor actor =
                adminWith(
                        AdminPermission.STORE_EDIT
                );

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/admin/store/settings/shipping")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        /*
         * STORE_EDIT no implica automáticamente STORE_VIEW.
         */
        mockMvc.perform(
                get("/admin/store/settings")
                        .with(actor)
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/store/settings/transfer")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void paymentMethodManageShouldOnlyGrantTransferSettings()
            throws Exception {

        RequestPostProcessor actor =
                adminWith(
                        AdminPermission
                                .PAYMENT_METHOD_MANAGE
                );

        mockMvc.perform(
                post("/admin/store/settings/transfer")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/store/settings/logo")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeBrandingEditShouldGrantBrandingMutationsOnly()
            throws Exception {

        RequestPostProcessor actor =
                adminWith(
                        AdminPermission
                                .STORE_BRANDING_EDIT
                );

        mockMvc.perform(
                post("/admin/store/settings")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/admin/store/settings/logo")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post(
                        "/admin/store/settings/logo/delete"
                )
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/admin/store/settings/shipping")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/admin/store/settings/transfer")
                        .with(actor)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unsupportedStoreSettingsOperationShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                patch("/admin/store/settings")
                        .with(
                                adminWith(
                                        AdminPermission.STORE_EDIT,
                                        AdminPermission.STORE_VIEW,
                                        AdminPermission
                                                .STORE_BRANDING_EDIT,
                                        AdminPermission
                                                .PAYMENT_METHOD_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    private RequestPostProcessor adminWith(
            AdminPermission... permissions
    ) {

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_STORE_ADMIN"
                )
        );

        for (AdminPermission permission : permissions) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            permission.authority()
                    )
            );
        }

        return user("admin@acme.test")
                .authorities(
                        authorities.toArray(
                                GrantedAuthority[]::new
                        )
                );
    }

    @RestController
    static class TestController {

        @GetMapping("/admin/store/settings")
        String settings() {
            return "OK";
        }

        @PostMapping("/admin/store/settings")
        String saveSettings() {
            return "OK";
        }

        @PostMapping("/admin/store/settings/shipping")
        String shipping() {
            return "OK";
        }

        @PostMapping("/admin/store/settings/transfer")
        String transfer() {
            return "OK";
        }

        @PostMapping("/admin/store/settings/logo")
        String logo() {
            return "OK";
        }

        @PostMapping("/admin/store/settings/logo/delete")
        String deleteLogo() {
            return "OK";
        }

        /*
         * Existe únicamente para comprobar que SecurityConfig
         * rechaza métodos futuros/no declarados.
         */
        @PatchMapping("/admin/store/settings")
        String unsupported() {
            return "OK";
        }
    }
}
