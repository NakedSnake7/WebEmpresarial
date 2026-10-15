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
                OrdersAdminAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class OrdersAdminAuthorizationMockMvcTest {

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
    void clientShouldNotAccessOrdersAdministration()
            throws Exception {

        mockMvc.perform(
                get("/orders")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                get("/orders/1")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void adminRoleAloneShouldNotGrantOrderAccess()
            throws Exception {

        mockMvc.perform(
                get("/orders")
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
    void orderViewShouldAllowReads()
            throws Exception {

        mockMvc.perform(
                get("/orders")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/orders/1")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void orderViewShouldAllowExports()
            throws Exception {

        mockMvc.perform(
                get("/admin/orders/excel")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                get("/admin/orders/pdf")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void orderViewShouldNotAllowMutations()
            throws Exception {

        mockMvc.perform(
                post("/orders/1/confirm-payment")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                post("/orders/1/cancel")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void orderUpdateShouldAllowDayToDayProcessing()
            throws Exception {

        RequestPostProcessor admin =
                adminWith(
                        AdminPermission.ORDER_UPDATE
                );

        mockMvc.perform(
                post("/orders/1/confirm-payment")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/orders/update-shipping")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/orders/1/status-ajax")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void orderUpdateShouldNotAllowCancellation()
            throws Exception {

        mockMvc.perform(
                post("/orders/1/cancel")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void orderCancelShouldAllowCancellation()
            throws Exception {

        mockMvc.perform(
                post("/orders/1/cancel")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_CANCEL
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void storeStaffPermissionsShouldNotAllowCancellation()
            throws Exception {

        mockMvc.perform(
                get("/orders")
                        .with(
                                staffWith(
                                        AdminPermission.ORDER_VIEW,
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/orders/1/status-ajax")
                        .with(
                                staffWith(
                                        AdminPermission.ORDER_VIEW,
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post("/orders/1/cancel")
                        .with(
                                staffWith(
                                        AdminPermission.ORDER_VIEW,
                                        AdminPermission.ORDER_UPDATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void unknownOrderOperationsShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                post("/orders/1/reopen")
                        .with(
                                adminWith(
                                        AdminPermission.ORDER_VIEW,
                                        AdminPermission.ORDER_UPDATE,
                                        AdminPermission.ORDER_CANCEL
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

        @GetMapping("/orders")
        String orders() {
            return "OK";
        }

        @GetMapping("/orders/{id}")
        String orderDetails() {
            return "OK";
        }

        @PostMapping("/orders/{id}/confirm-payment")
        String confirmPayment() {
            return "OK";
        }

        @PostMapping("/orders/update-shipping")
        String updateShipping() {
            return "OK";
        }

        @PostMapping("/orders/{id}/cancel")
        String cancelOrder() {
            return "OK";
        }

        @PostMapping("/orders/{id}/status-ajax")
        String updateStatus() {
            return "OK";
        }

        @PostMapping("/orders/{id}/reopen")
        String reopenOrder() {
            return "OK";
        }

        @GetMapping("/admin/orders/excel")
        String excel() {
            return "OK";
        }

        @GetMapping("/admin/orders/pdf")
        String pdf() {
            return "OK";
        }
    }
}
