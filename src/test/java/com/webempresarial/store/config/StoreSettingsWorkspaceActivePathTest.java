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
class StoreSettingsWorkspaceActivePathTest {

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
    void crmSettingsShouldKeepSettingsWorkspaceActive() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/store/crm",
                        "/admin/store/crm/budget-ranges",
                        "/admin/store/crm/pipeline",
                        "/admin/store/crm/automation"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");
    }

    @Test
    void storeSettingsShouldKeepTheirOwnWorkspacePath() {

        when(request.getRequestURI())
                .thenReturn("/admin/store/settings");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/store/settings");
    }

    @Test
    void domainAndBillingShouldRemainOutsideSettingsWorkspace() {

        when(request.getRequestURI())
                .thenReturn(
                        "/admin/domains",
                        "/admin/billing"
                );

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/domains");

        assertThat(advice.sidebarActivePath(request))
                .isEqualTo("/admin/billing");
    }
}
