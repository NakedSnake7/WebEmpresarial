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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                CatalogAdminAuthorizationMockMvcTest.TestController.class
)
@Import(SecurityConfig.class)
class CatalogAdminAuthorizationMockMvcTest {

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
    void adminRoleAloneShouldNotGrantCatalogAccess()
            throws Exception {

        mockMvc.perform(
                get("/admin/productos")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/nuevo")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void productViewShouldAllowProductReads()
            throws Exception {

        mockMvc.perform(
                get("/admin/productos")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/productos")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/productos/1")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void productViewShouldNotGrantProductMutation()
            throws Exception {

        mockMvc.perform(
                post("/api/productos/toggleVisibility")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                delete("/api/productos/1")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void productCreateShouldAllowNewProductFlow()
            throws Exception {

        mockMvc.perform(
                get("/nuevo")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_CREATE
                                )
                        )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/nuevo")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_CREATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void productEditShouldAllowEditAndPricingOperations()
            throws Exception {

        RequestPostProcessor editor =
                adminWith(
                        AdminPermission.PRODUCT_EDIT
                );

        mockMvc.perform(
                get("/editar/1")
                        .with(editor)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/editar/1")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/modificar-precios")
                        .with(editor)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/editar/1")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/togglePromocion")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/toggleVisibility")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/toggleCategoria")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/actualizarPrecio")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/ajustarPrecioCategoria")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/variantes/actualizarPrecio")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/api/productos/1/eliminar-imagen/2")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void productEditShouldNotGrantProductDeletion()
            throws Exception {

        mockMvc.perform(
                delete("/api/productos/1")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void productDeleteShouldAllowDeletingProductButNotEditingImage()
            throws Exception {

        mockMvc.perform(
                delete("/api/productos/1")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_DELETE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/api/productos/1/eliminar-imagen/2")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_DELETE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void inventoryEditShouldAllowVariantStockMutation()
            throws Exception {

        mockMvc.perform(
                post("/variantes/1/stock")
                        .with(
                                adminWith(
                                        AdminPermission.INVENTORY_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void productEditShouldNotGrantVariantStockMutation()
            throws Exception {

        mockMvc.perform(
                post("/variantes/1/stock")
                        .with(
                                adminWith(
                                        AdminPermission.PRODUCT_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void salesProductViewShouldRemainReadOnly()
            throws Exception {

        mockMvc.perform(
                get("/admin/productos")
                        .with(
                                salesWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/productos/1")
                        .with(
                                salesWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/productos/toggleVisibility")
                        .with(
                                salesWith(
                                        AdminPermission.PRODUCT_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void staffOperationalPermissionsShouldNotGrantProductDeletion()
            throws Exception {

        RequestPostProcessor staff =
                staffWith(
                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.PRODUCT_CREATE,
                        AdminPermission.PRODUCT_EDIT,
                        AdminPermission.INVENTORY_VIEW,
                        AdminPermission.INVENTORY_EDIT
                );

        mockMvc.perform(
                get("/admin/productos")
                        .with(staff)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/nuevo")
                        .with(staff)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/variantes/actualizarPrecio")
                        .with(staff)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/variantes/1/stock")
                        .with(staff)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                delete("/api/productos/1")
                        .with(staff)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unknownCatalogOperationsShouldFailClosed()
            throws Exception {

        RequestPostProcessor privileged =
                adminWith(
                        AdminPermission.PRODUCT_VIEW,
                        AdminPermission.PRODUCT_CREATE,
                        AdminPermission.PRODUCT_EDIT,
                        AdminPermission.PRODUCT_DELETE,
                        AdminPermission.INVENTORY_EDIT
                );

        mockMvc.perform(
                get("/admin/categorias")
                        .with(privileged)
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/subirProducto")
                        .with(privileged)
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/api/productos/rebuild-index")
                        .with(privileged)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/api/variantes/rebuild")
                        .with(privileged)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
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

    private RequestPostProcessor salesWith(
            AdminPermission... permissions
    ) {
        return userWithRole(
                "SALES",
                "sales@stride.test",
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

        @GetMapping("/admin/productos")
        String products() {
            return "OK";
        }

        @GetMapping("/admin/categorias")
        String categoriesAdmin() {
            return "OK";
        }

        @GetMapping("/subirProducto")
        String legacyUploadProduct() {
            return "OK";
        }

        @GetMapping("/nuevo")
        String newProduct() {
            return "OK";
        }

        @PostMapping("/nuevo")
        String createProduct() {
            return "OK";
        }

        @GetMapping("/editar/{id}")
        String editProduct() {
            return "OK";
        }

        @PostMapping("/editar/{id}")
        String updateProductView() {
            return "OK";
        }

        @GetMapping("/modificar-precios")
        String prices() {
            return "OK";
        }

        @PostMapping("/variantes/{id}/stock")
        String updateStock() {
            return "OK";
        }

        @GetMapping("/api/productos")
        String apiProducts() {
            return "OK";
        }

        @GetMapping("/api/productos/{id}")
        String apiProduct() {
            return "OK";
        }

        @DeleteMapping("/api/productos/{id}")
        String deleteProduct() {
            return "OK";
        }

        @PostMapping("/api/productos/editar/{id}")
        String updateProduct() {
            return "OK";
        }

        @DeleteMapping(
                "/api/productos/{productId}/eliminar-imagen/{imageId}"
        )
        String deleteImage() {
            return "OK";
        }

        @PostMapping("/api/productos/togglePromocion")
        String togglePromotion() {
            return "OK";
        }

        @PostMapping("/api/productos/toggleVisibility")
        String toggleVisibility() {
            return "OK";
        }

        @PostMapping("/api/productos/toggleCategoria")
        String toggleCategory() {
            return "OK";
        }

        @PostMapping("/api/productos/actualizarPrecio")
        String updatePrice() {
            return "OK";
        }

        @PostMapping("/api/productos/ajustarPrecioCategoria")
        String adjustCategoryPrice() {
            return "OK";
        }

        @PostMapping("/api/variantes/actualizarPrecio")
        String updateVariantPrice() {
            return "OK";
        }

        @PostMapping("/api/productos/rebuild-index")
        String rebuildIndex() {
            return "OK";
        }

        @PostMapping("/api/variantes/rebuild")
        String rebuildVariants() {
            return "OK";
        }
    }
}
