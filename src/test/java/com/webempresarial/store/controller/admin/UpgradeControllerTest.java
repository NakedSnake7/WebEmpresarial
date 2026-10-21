package com.webempresarial.store.controller.admin;

import com.webempresarial.store.feature.FeatureAccessPolicy;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.service.FeatureAccessService;

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
    private UpgradeController controller;

    @BeforeEach
    void setUp() {

        featureAccessService =
                mock(FeatureAccessService.class);

        controller =
                new UpgradeController(
                        featureAccessService
                );
    }

    @Test
    void featureShouldUseCanonicalMinimumPlan() {

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

        Model model =
                new ExtendedModelMap();

        String view =
                controller.upgrade(
                        Feature.MULTI_USER,
                        model
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

        verify(featureAccessService)
                .definition(
                        Feature.MULTI_USER
                );
    }

    @Test
    void missingFeatureShouldDefaultToProPlan() {

        Model model =
                new ExtendedModelMap();

        controller.upgrade(
                null,
                model
        );

        assertThat(
                model.getAttribute("requiredPlan")
        ).isEqualTo(
                StorePlan.PRO
        );

        verifyNoInteractions(
                featureAccessService
        );
    }
}
