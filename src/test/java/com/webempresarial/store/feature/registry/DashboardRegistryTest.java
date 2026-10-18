package com.webempresarial.store.feature.registry;

import com.webempresarial.store.dto.dashboard.DashboardWidgetDTO;
import com.webempresarial.store.feature.FeatureCategory;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.PlatformModuleDescriptor;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardRegistryTest {

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    private DashboardRegistry dashboardRegistry;
    private Store store;

    @BeforeEach
    void setUp() {

        dashboardRegistry =
                new DashboardRegistry(
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        store = new Store();
        store.setId(3L);

        registerAnalytics();
    }

    @Test
    void shouldHideAnalyticsWithoutPermission() {

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(false);

        assertThat(
                dashboardRegistry.widgets(store)
        ).isEmpty();
    }

    @Test
    void shouldShowAnalyticsWhenPermissionAndFeatureAvailable() {

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ANALYTICS
                )
        ).thenReturn(true);

        List<DashboardWidgetDTO> widgets =
                dashboardRegistry.widgets(store);

        assertThat(widgets)
                .hasSize(1);

        DashboardWidgetDTO widget =
                widgets.get(0);

        assertThat(widget.locked())
                .isFalse();

        assertThat(widget.url())
                .isEqualTo("/crm/reports");
    }

    @Test
    void shouldLockAnalyticsWhenPermissionButFeatureUnavailable() {

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.ANALYTICS_VIEW
                )
        ).thenReturn(true);

        when(
                featureAccessService.canUse(
                        store,
                        Feature.ANALYTICS
                )
        ).thenReturn(false);

        List<DashboardWidgetDTO> widgets =
                dashboardRegistry.widgets(store);

        assertThat(widgets)
                .hasSize(1);

        DashboardWidgetDTO widget =
                widgets.get(0);

        assertThat(widget.locked())
                .isTrue();

        assertThat(widget.url())
                .isEqualTo(
                        "/admin/upgrade?feature=ANALYTICS"
                );
    }

    private void registerAnalytics() {

        FeatureDefinition analytics =
                FeatureDefinition
                        .builder(Feature.ANALYTICS)
                        .displayName("Analytics")
                        .description(
                                "Consulta reportes y métricas avanzadas."
                        )
                        .category(
                                FeatureCategory.PLATFORM
                        )
                        .icon("📈")
                        .url("/crm/reports")
                        .order(150)
                        .presentation(
                                FeaturePresentation
                                        .builder()
                                        .showInDashboard(true)
                                        .build()
                        )
                        .dashboardPermission(
                                AdminPermission.ANALYTICS_VIEW
                        )
                        .build();

        dashboardRegistry.register(
                PlatformModuleDescriptor
                        .builder("Platform")
                        .feature(analytics)
                        .build()
        );
    }
}
