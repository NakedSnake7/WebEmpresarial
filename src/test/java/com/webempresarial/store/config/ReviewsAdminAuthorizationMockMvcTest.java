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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                ReviewsAdminAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class ReviewsAdminAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService storeSettingsService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver storeThemeResolver;

    @MockitoBean
    private FeatureAccessService featureAccessService;

    @Test
    void clientShouldNotAccessReviewAdministration()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/resenas/nueva")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void clientShouldNotMutateReviews()
            throws Exception {

        mockMvc.perform(
                post("/resenas/nueva")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                put("/resenas/1")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                delete("/resenas/eliminar/1")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeAdminShouldAccessReviewAdministration()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(user("admin@stride.test")
                                .roles("STORE_ADMIN"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/resenas/nueva")
                        .with(user("admin@stride.test")
                                .roles("STORE_ADMIN"))
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeStaffShouldAccessReviewAdministration()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(user("staff@stride.test")
                                .roles("STORE_STAFF"))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1")
                        .with(user("staff@stride.test")
                                .roles("STORE_STAFF"))
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @RestController
    static class TestController {

        @GetMapping("/resenas")
        String reviews() {
            return "OK";
        }

        @GetMapping("/resenas/nueva")
        String newReview() {
            return "OK";
        }

        @PostMapping("/resenas/nueva")
        String createReview() {
            return "OK";
        }

        @PutMapping("/resenas/{id}")
        String updateReview() {
            return "OK";
        }

        @DeleteMapping("/resenas/eliminar/{id}")
        String deleteReview() {
            return "OK";
        }
    }
}
