package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceWorkspaceActivePathTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private SidebarRegistry sidebarRegistry;

    @Mock
    private InventoryPersistentAlertService
            inventoryAlertService;

    @Mock
    private ObjectProvider<FeatureAccessService>
            featureAccessServiceProvider;

    @Mock
    private HttpServletRequest request;

    private FeatureViewAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new FeatureViewAdvice(
                storeContextService,
                sidebarRegistry,
                inventoryAlertService,
                featureAccessServiceProvider
        );
    }

    @Test
    void productActionsShouldKeepProductsWorkspaceActive() {

        when(request.getRequestURI())
                .thenReturn(
                        "/nuevo",
                        "/modificar-precios",
                        "/editar/42"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/productos");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/productos");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/productos");
    }

    @Test
    void nestedWorkspaceRoutesShouldKeepTheirOwnPath() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/productos/42/inventory",
                        "/admin/inventory/movements",
                        "/admin/inventory/alerts"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo(
                        "/admin/productos/42/inventory"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo(
                        "/admin/inventory/movements"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo(
                        "/admin/inventory/alerts"
                );
    }
}
