package com.webempresarial.store.controller.admin;

import java.time.LocalDateTime;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StripeBillingPortalService;
import com.webempresarial.store.service.StripeBillingService;
import com.webempresarial.store.service.StripeSubscriptionChangeService;
import com.webempresarial.store.theme.ThemeModelAdvice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        controllers = BillingController.class,
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
class BillingTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreContextService storeContextService;

    @MockitoBean
    private StripeBillingService stripeBillingService;

    @MockitoBean
    private StripeBillingPortalService stripeBillingPortalService;

    @MockitoBean
    private PlatformAccessService platformAccessService;

    @MockitoBean
    private StripeSubscriptionChangeService stripeSubscriptionChangeService;

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
    void shouldRenderScheduledPlanChangeOnlyOnce() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setPendingPlan(StorePlan.PRO);
        subscription.setPendingPlanEffectiveAt(
                LocalDateTime.of(2026, 10, 15, 12, 30)
        );

        String html = renderBilling(subscription);

        assertThat(html)
                .containsOnlyOnce("Cambio programado a")
                .contains(">PRO</strong>")
                .contains("15/10/2026 12:30");
    }

    @Test
    void shouldRenderScheduledPlanChangeWithoutEffectiveDate()
            throws Exception {

        Subscription subscription = new Subscription();
        subscription.setPendingPlan(StorePlan.PRO);

        String html = renderBilling(subscription);

        assertThat(html)
                .containsOnlyOnce("Cambio programado a")
                .contains(">PRO</strong>")
                .doesNotContain("a partir del");
    }

    private String renderBilling(Subscription subscription)
            throws Exception {

        Store store = new Store();
        store.setSubscription(subscription);
        subscription.setStore(store);

        when(
                storeContextService.getCurrentStore(any())
        ).thenReturn(store);

        when(
                platformAccessService.resolveEffectivePlan(store)
        ).thenReturn(StorePlan.BASIC);

        MvcResult result = mockMvc.perform(
                        get("/admin/billing")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/billing/index"))
                .andReturn();

        return result.getResponse().getContentAsString();
    }
}
