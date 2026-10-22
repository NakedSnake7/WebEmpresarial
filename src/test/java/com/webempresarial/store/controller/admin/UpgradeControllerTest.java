package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.FeatureAccessPolicy;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UpgradeControllerTest {

    private FeatureAccessService featureAccessService;
    private StoreContextService storeContextService;
    private PlatformAccessService platformAccessService;

    private HttpServletRequest request;
    private Store store;

    private UpgradeController controller;

    @BeforeEach
    void setUp() {

        featureAccessService =
                mock(FeatureAccessService.class);

        storeContextService =
                mock(StoreContextService.class);

        platformAccessService =
                mock(PlatformAccessService.class);

        request =
                mock(HttpServletRequest.class);

        store =
                mock(Store.class);

        controller =
                new UpgradeController(
                        featureAccessService,
                        storeContextService,
                        platformAccessService
                );

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(store);
    }

    @Test
    void featureShouldUseCanonicalMinimumPlanAndRequireUpgradeWhenHigher() {

        FeatureDefinition definition =
                mock(FeatureDefinition.class);

        FeatureAccessPolicy policy =
                FeatureAccessPolicy.builder()
                        /*
                         * PREMIUM es intencional.
                         *
                         * MULTI_USER actualmente pertenece a PRO.
                         * Si el controller tuviera una matriz local,
                         * este test devolvería PRO y fallaría.
                         */
                        .minimumPlan(StorePlan.PREMIUM)
                        .premium(true)
                        .enabled(true)
                        .build();

        when(
                featureAccessService.definition(
                        Feature.MULTI_USER
                )
        ).thenReturn(definition);

        when(
                definition.getAccessPolicy()
        ).thenReturn(policy);

        when(
                platformAccessService.resolveEffectivePlan(store)
        ).thenReturn(StorePlan.PRO);

        Model model =
                new ExtendedModelMap();

        String view =
                controller.upgrade(
                        Feature.MULTI_USER,
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo(
                        "admin/upgrade/index"
                );

        assertThat(
                model.getAttribute("feature")
        ).isEqualTo(
                Feature.MULTI_USER
        );

        assertThat(
                model.getAttribute("requiredPlan")
        ).isEqualTo(
                StorePlan.PREMIUM
        );

        assertThat(
                model.getAttribute("currentPlan")
        ).isEqualTo(
                StorePlan.PRO
        );

        assertThat(
                model.getAttribute("upgradeRequired")
        ).isEqualTo(
                true
        );

        verify(featureAccessService)
                .definition(
                        Feature.MULTI_USER
                );
    }

    @Test
    void featureAlreadyCoveredByCurrentPlanShouldNotRequireUpgrade() {

        FeatureDefinition definition =
                mock(FeatureDefinition.class);

        FeatureAccessPolicy policy =
                FeatureAccessPolicy.builder()
                        .minimumPlan(StorePlan.PRO)
                        .premium(true)
                        .enabled(true)
                        .build();

        when(
                featureAccessService.definition(
                        Feature.PIPELINE
                )
        ).thenReturn(definition);

        when(
                definition.getAccessPolicy()
        ).thenReturn(policy);

        when(
                platformAccessService.resolveEffectivePlan(store)
        ).thenReturn(StorePlan.PREMIUM);

        Model model =
                new ExtendedModelMap();

        controller.upgrade(
                Feature.PIPELINE,
                model,
                request
        );

        assertThat(
                model.getAttribute("requiredPlan")
        ).isEqualTo(
                StorePlan.PRO
        );

        assertThat(
                model.getAttribute("currentPlan")
        ).isEqualTo(
                StorePlan.PREMIUM
        );

        assertThat(
                model.getAttribute("upgradeRequired")
        ).isEqualTo(
                false
        );
    }

    @Test
    void missingFeatureShouldDefaultToProPlan() {

        when(
                platformAccessService.resolveEffectivePlan(store)
        ).thenReturn(StorePlan.BASIC);

        Model model =
                new ExtendedModelMap();

        controller.upgrade(
                null,
                model,
                request
        );

        assertThat(
                model.getAttribute("requiredPlan")
        ).isEqualTo(
                StorePlan.PRO
        );

        assertThat(
                model.getAttribute("currentPlan")
        ).isEqualTo(
                StorePlan.BASIC
        );

        assertThat(
                model.getAttribute("upgradeRequired")
        ).isEqualTo(
                true
        );

        verifyNoInteractions(
                featureAccessService
        );
    }
}
