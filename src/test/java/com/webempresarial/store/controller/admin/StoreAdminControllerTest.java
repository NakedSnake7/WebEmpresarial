package com.webempresarial.store.controller.admin;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.Store;
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
        controllers = StoreAdminController.class,
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
class StoreAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldRenderStoreList() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");

        List<Store> stores = List.of(store);

        when(
                storeAdminService.listarTiendas()
        ).thenReturn(stores);

        mockMvc.perform(
                get("/admin/stores")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/store/list"))
        .andExpect(model().attribute("stores", stores));

        verify(storeAdminService).listarTiendas();
    }

    @Test
    void shouldRenderNewStoreForm() throws Exception {
        mockMvc.perform(
                get("/admin/stores/nuevo")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/store/form"))
        .andExpect(model().attributeExists("store"));
    }

    @Test
    void shouldRenderEditStoreForm() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");

        when(
                storeAdminService.buscarPorId(42L)
        ).thenReturn(store);

        mockMvc.perform(
                get("/admin/stores/editar/42")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/store/form"))
        .andExpect(model().attribute("store", store));

        verify(storeAdminService).buscarPorId(42L);
    }
}
