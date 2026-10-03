package com.webempresarial.store.controller.admin;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.repository.SubscriptionRepository;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.SubscriptionService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = AdminSubscriptionController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                StoreViewAdvice.class,
                                ThemeModelAdvice.class
                        }
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private StoreRepository storeRepository;

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
    void shouldLoadSubscriptionsAndStores() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");
        store.setDominio("stride.local");

        Subscription subscription = new Subscription();
        subscription.setStore(store);

        List<Subscription> subscriptions =
                List.of(subscription);

        List<Store> stores =
                List.of(store);

        when(
                subscriptionRepository.findAllWithStore()
        ).thenReturn(subscriptions);

        when(
                storeRepository.findAll()
        ).thenReturn(stores);

        mockMvc.perform(
                get("/admin/subscriptions")
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                view().name(
                        "admin/subscriptions/list"
                )
        )
        .andExpect(
                model().attribute(
                        "subscriptions",
                        subscriptions
                )
        )
        .andExpect(
                model().attribute(
                        "stores",
                        stores
                )
        );

        verify(subscriptionRepository)
                .findAllWithStore();

        verify(storeRepository)
                .findAll();
    }

    @Test
    void shouldCreateInternalSubscription() throws Exception {
        Store store = new Store();
        store.setId(42L);
        store.setNombre("STRIDE");
        store.setDominio("stride.local");

        when(
                storeRepository.findById(42L)
        ).thenReturn(
                Optional.of(store)
        );

        mockMvc.perform(
                post("/admin/subscriptions/internal/create")
                        .param(
                                "storeId",
                                "42"
                        )
                        .param(
                                "plan",
                                "PRO"
                        )
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/subscriptions"
                )
        );

        verify(storeRepository)
                .findById(42L);

        verify(subscriptionService)
                .createInternalSubscription(
                        store,
                        StorePlan.PRO
                );
    }
}
