package com.webempresarial.store.controller.admin;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.StoreAdminService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = AdminStoreUserController.class,
        excludeFilters = {
                @Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                StoreViewAdvice.class,
                                ThemeModelAdvice.class
                        }
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminStoreUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private StoreAdminService storeAdminService;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private SidebarRegistry sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService inventoryPersistentAlertService;

    @MockitoBean
    private AdminTenantAccessInterceptor adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor subscriptionInterceptor;

    @BeforeEach
    void allowMvcInterceptors() throws Exception {
        when(
                adminTenantAccessInterceptor.preHandle(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(true);

        when(
                subscriptionInterceptor.preHandle(
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(true);
    }

    @Test
    void shouldRenderStoreAdminsList() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");

        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setFullName("Admin STRIDE");
        admin.setEmail("admin@stride.test");

        List<AdminUser> admins = List.of(admin);

        when(
                storeAdminService.buscarPorId(42L)
        ).thenReturn(store);

        when(
                adminUserService.listarPorTienda(42L)
        ).thenReturn(admins);

        mockMvc.perform(
                get("/admin/stores/42/admins")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/store/admins/list"))
        .andExpect(model().attribute("store", store))
        .andExpect(model().attribute("admins", admins));

        verify(storeAdminService).buscarPorId(42L);
        verify(adminUserService).listarPorTienda(42L);
    }

    @Test
    void shouldRenderNewStoreAdminForm() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");

        AdminUser adminUser = new AdminUser();

        when(
                storeAdminService.buscarPorId(42L)
        ).thenReturn(store);

        when(
                adminUserService.nuevoAdmin(42L)
        ).thenReturn(adminUser);

        mockMvc.perform(
                get("/admin/stores/42/admins/nuevo")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/store/admins/form"))
        .andExpect(model().attribute("store", store))
        .andExpect(model().attribute("adminUser", adminUser))
        .andExpect(model().attributeExists("roles"));

        verify(storeAdminService).buscarPorId(42L);
        verify(adminUserService).nuevoAdmin(42L);
    }
}
