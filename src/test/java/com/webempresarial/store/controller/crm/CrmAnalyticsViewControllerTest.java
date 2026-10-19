package com.webempresarial.store.controller.crm;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.ExtendedModelMap;

import com.webempresarial.store.dto.lead.CrmDashboardDTO;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CrmDashboardService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.FeatureUsageService;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.service.StoreContextService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmAnalyticsViewControllerTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private FeatureUsageService featureUsageService;

    @Mock
    private LeadService leadService;

    @Mock
    private CrmDashboardService crmDashboardService;

    @Mock
    private HttpServletRequest request;

    private Store store;

    private CrmViewController controller;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(store);

        controller =
                new CrmViewController(
                        storeContextService,
                        featureAccessService,
                        featureUsageService,
                        leadService,
                        crmDashboardService
                );
    }

    @Test
    void reportsShouldRequireAnalyticsAndExposeMetrics() {

        CrmDashboardDTO metrics =
                new CrmDashboardDTO(
                        3L,
                        12L,
                        31L,
                        5L,
                        7L,
                        2L,
                        4L,
                        6L,
                        new BigDecimal("15000.00"),
                        new BigDecimal("15000.00"),
                        new BigDecimal("25.00"),
                        "Instagram",
                        "Sin asignar"
                );

        when(
                crmDashboardService.getDashboard(10L)
        ).thenReturn(metrics);

        ExtendedModelMap model =
                new ExtendedModelMap();

        String view =
                controller.reports(
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo("crm/reports");

        assertThat(model.get("crmPage"))
                .isEqualTo(true);

        assertThat(model.get("analytics"))
                .isSameAs(metrics);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.ANALYTICS
                );

        verify(featureUsageService)
                .track(
                        store,
                        Feature.ANALYTICS,
                        "crm_reports"
                );

        verify(crmDashboardService)
                .getDashboard(10L);
    }

    @Test
    void lockedAnalyticsShouldStopMetricsService() {

        doThrow(
                new FeatureLockedException(
                        Feature.ANALYTICS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.ANALYTICS
        );

        assertThatThrownBy(
                () -> controller.reports(
                        new ExtendedModelMap(),
                        request
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                featureUsageService,
                crmDashboardService
        );
    }
}
