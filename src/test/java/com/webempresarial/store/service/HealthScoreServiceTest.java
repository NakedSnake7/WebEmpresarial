package com.webempresarial.store.service;

import com.webempresarial.store.commerce.application.order.OrderMetricsQueryService;
import com.webempresarial.store.dto.saas.StoreHealthDTO;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.SubscriptionStatus;
import com.webempresarial.store.repository.FeatureUsageRepository;
import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthScoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private OrderMetricsQueryService orderMetricsQueryService;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private FeatureUsageRepository featureUsageRepository;

    @Mock
    private Store store;

    @Mock
    private Subscription subscription;

    private HealthScoreService healthScoreService;

    @BeforeEach
    void setUp() {
        healthScoreService =
                new HealthScoreService(
                        storeRepository,
                        productoRepository,
                        orderMetricsQueryService,
                        leadRepository,
                        featureUsageRepository
                );
    }

    @Test
    void implementedDimensionsShouldNormalizeNinetyPointsToOneHundred() {

        when(store.getId()).thenReturn(10L);
        when(store.getNombre()).thenReturn("Demo");
        when(store.getDominio()).thenReturn("demo.test");

        when(store.getLogoUrl()).thenReturn("logo.png");
        when(store.getFaviconUrl()).thenReturn("favicon.ico");
        when(store.getHeroImageUrl()).thenReturn("hero.jpg");
        when(store.getPrimaryColor()).thenReturn("#111111");
        when(store.getSecondaryColor()).thenReturn("#222222");
        when(store.getAccentColor()).thenReturn("#333333");
        when(store.getSlogan()).thenReturn("Demo");

        when(store.getSubscription()).thenReturn(subscription);
        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.ACTIVE);
        when(subscription.isBillingExempt())
                .thenReturn(true);
        when(subscription.getStripeCustomerId())
                .thenReturn("cus_test");
        when(subscription.getStripeSubscriptionId())
                .thenReturn("sub_test");

        when(store.isStripeConnected())
                .thenReturn(true);

        when(productoRepository.countByStoreId(10L))
                .thenReturn(5L);
        when(
                productoRepository
                        .countByStoreIdAndVisibleEnMenuTrue(10L)
        ).thenReturn(5L);

        when(orderMetricsQueryService.countOrdersByStore(10L))
                .thenReturn(1L);

        when(leadRepository.countAllLeads(10L))
                .thenReturn(1L);
        when(leadRepository.countClosedLeads(10L))
                .thenReturn(1L);

        when(
                featureUsageRepository.countByStoreAndFeature(
                        store,
                        Feature.CRM
                )
        ).thenReturn(1L);

        when(
                featureUsageRepository.countByStoreAndFeature(
                        store,
                        Feature.PIPELINE
                )
        ).thenReturn(1L);

        when(
                featureUsageRepository.countByStoreAndFeature(
                        store,
                        Feature.ANALYTICS
                )
        ).thenReturn(1L);

        when(
                featureUsageRepository.countByStoreAndUsedAtAfter(
                        any(Store.class),
                        any(LocalDateTime.class)
                )
        ).thenReturn(500L);

        StoreHealthDTO result =
                healthScoreService.calculate(store);

        assertThat(result.getAutomationScore())
                .isZero();

        assertThat(result.getOverallScore())
                .isEqualTo(100);

        assertThat(result.getStatus())
                .isEqualTo("HEALTHY");

        assertThat(result.getRecommendations())
                .doesNotContain(
                        "Activa Automatizaciones.",
                        "Configura campañas de Email Marketing.",
                        "Implementa automatizaciones por WhatsApp."
                );
    }

    @Test
    void futureAutomationFeaturesShouldNotParticipateInHealthCalculation() {

        when(store.getId()).thenReturn(10L);
        when(store.getNombre()).thenReturn("Demo");
        when(store.getDominio()).thenReturn("demo.test");

        healthScoreService.calculate(store);

        verify(featureUsageRepository, never())
                .countByStoreAndFeature(
                        store,
                        Feature.AUTOMATIONS
                );

        verify(featureUsageRepository, never())
                .countByStoreAndFeature(
                        store,
                        Feature.EMAIL_MARKETING
                );

        verify(featureUsageRepository, never())
                .countByStoreAndFeature(
                        store,
                        Feature.WHATSAPP_AUTOMATION
                );
    }
}
