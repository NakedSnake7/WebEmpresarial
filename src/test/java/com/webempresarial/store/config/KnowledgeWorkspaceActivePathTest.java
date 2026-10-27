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
class KnowledgeWorkspaceActivePathTest {

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
    void knowledgeRoutesShouldKeepKnowledgeWorkspaceActive() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/knowledge",
                        "/admin/knowledge/library",
                        "/admin/knowledge/new",
                        "/admin/knowledge/42",
                        "/admin/knowledge/42/versions/new",
                        "/admin/knowledge/42/versions/7"
                );

        for (int i = 0; i < 6; i++) {
            assertThat(
                    advice.sidebarActivePath(request)
            ).isEqualTo("/admin/knowledge");
        }
    }

    @Test
    void unrelatedWorkspacesShouldRemainIndependent() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/productos",
                        "/crm/dashboard",
                        "/admin/store/settings"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/productos");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");
    }
}
