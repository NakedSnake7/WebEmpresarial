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
class CrmWorkspaceActivePathTest {

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
    void crmSubsectionsShouldKeepCrmWorkspaceActive() {

        when(request.getRequestURI())
                .thenReturn(
                        "/crm/pipeline",
                        "/crm/tasks",
                        "/admin/leads",
                        "/admin/leads/42",
                        "/crm/leads/42"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/dashboard");
    }

    @Test
    void analyticsShouldRemainOutsideCrmWorkspace() {

        when(request.getRequestURI())
                .thenReturn("/crm/reports");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/crm/reports");
    }

    @Test
    void crmStoreSettingsShouldRemainOutsideCrmWorkspace() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/store/crm/pipeline"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo(
                        "/admin/store/crm/pipeline"
                );
    }
}
