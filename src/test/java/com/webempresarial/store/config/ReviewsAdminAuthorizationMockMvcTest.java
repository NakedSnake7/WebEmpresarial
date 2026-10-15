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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                post("/resenas/nueva")
                        .with(user("cliente@test.local")
                                .roles("CLIENTE"))
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void roleWithoutReviewAuthoritiesShouldNotGrantAccess()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(user("admin@stride.test")
                                .roles("STORE_ADMIN"))
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/resenas")
                        .with(user("staff@stride.test")
                                .roles("STORE_STAFF"))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void reviewViewShouldOnlyGrantReviewListing()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(reviewUser(
                                "viewer@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_VIEW
                        ))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/resenas/nueva")
                        .with(reviewUser(
                                "viewer@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_VIEW
                        ))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void reviewCreateShouldGrantNewFormAndCreation()
            throws Exception {

        mockMvc.perform(
                get("/resenas/nueva")
                        .with(reviewUser(
                                "creator@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_CREATE
                        ))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/resenas/nueva")
                        .with(reviewUser(
                                "creator@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_CREATE
                        ))
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void reviewEditShouldGrantContentAndImageMutations()
            throws Exception {

        var editor =
                reviewUser(
                        "editor@stride.test",
                        "ROLE_STORE_STAFF",
                        AdminPermission.REVIEW_EDIT
                );

        mockMvc.perform(
                put("/resenas/1")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1/imagen")
                        .with(reviewUser(
                                "editor@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_EDIT
                        ))
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/resenas/1/imagen")
                        .with(reviewUser(
                                "editor@stride.test",
                                "ROLE_STORE_STAFF",
                                AdminPermission.REVIEW_EDIT
                        ))
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void reviewDeleteShouldGrantReviewDeletion()
            throws Exception {

        mockMvc.perform(
                delete("/resenas/eliminar/1")
                        .with(reviewUser(
                                "admin@stride.test",
                                "ROLE_STORE_ADMIN",
                                AdminPermission.REVIEW_DELETE
                        ))
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void storeStaffShouldOperateReviewsButNotDeleteThem()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(staffUser())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/resenas/nueva")
                        .with(staffUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1")
                        .with(staffUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1/imagen")
                        .with(staffUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/resenas/1/imagen")
                        .with(staffUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/resenas/eliminar/1")
                        .with(staffUser())
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void storeAdminShouldHaveFullReviewAdministration()
            throws Exception {

        mockMvc.perform(
                get("/resenas")
                        .with(adminUser())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/resenas/nueva")
                        .with(adminUser())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/resenas/nueva")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                put("/resenas/1/imagen")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/resenas/1/imagen")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/resenas/eliminar/1")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void unsupportedReviewEndpointShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                patch("/resenas/1")
                        .with(adminUser())
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    private static org.springframework.security.test.web.servlet.request
            .SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor
            reviewUser(
                    String username,
                    String role,
                    AdminPermission... permissions
            ) {

        SimpleGrantedAuthority[] authorities =
                new SimpleGrantedAuthority[
                        permissions.length + 1
                ];

        authorities[0] =
                new SimpleGrantedAuthority(role);

        for (int i = 0; i < permissions.length; i++) {
            authorities[i + 1] =
                    new SimpleGrantedAuthority(
                            permissions[i].authority()
                    );
        }

        return user(username)
                .authorities(authorities);
    }

    private static org.springframework.security.test.web.servlet.request
            .SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor
            staffUser() {

        return reviewUser(
                "staff@stride.test",
                "ROLE_STORE_STAFF",
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT
        );
    }

    private static org.springframework.security.test.web.servlet.request
            .SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor
            adminUser() {

        return reviewUser(
                "admin@stride.test",
                "ROLE_STORE_ADMIN",
                AdminPermission.REVIEW_VIEW,
                AdminPermission.REVIEW_CREATE,
                AdminPermission.REVIEW_EDIT,
                AdminPermission.REVIEW_DELETE
        );
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

        @PutMapping("/resenas/{id}/imagen")
        String updateReviewImage() {
            return "OK";
        }

        @DeleteMapping("/resenas/{id}/imagen")
        String deleteReviewImage() {
            return "OK";
        }

        @DeleteMapping("/resenas/eliminar/{id}")
        String deleteReview() {
            return "OK";
        }

        @PatchMapping("/resenas/{id}")
        String unsupportedReviewMutation() {
            return "OK";
        }
    }
}
