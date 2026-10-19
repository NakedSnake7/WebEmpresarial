package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketingFeaturesPresentationContractTest {

    @Test
    void emailMarketingShouldRemainPremiumButHiddenUntilImplemented() {

        FeatureDefinition definition =
                definitionFor(Feature.EMAIL_MARKETING);

        assertThat(
                definition
                        .getAccessPolicy()
                        .getMinimumPlan()
        ).isEqualTo(StorePlan.PREMIUM);

        FeaturePresentation presentation =
                definition.getPresentation();

        assertThat(presentation.isShowInSidebar())
                .isFalse();

        assertThat(presentation.isShowInDashboard())
                .isFalse();

        assertThat(presentation.isShowInBilling())
                .isTrue();

        assertThat(presentation.isShowUpgradeCard())
                .isTrue();

        assertThat(presentation.isTrackUsage())
                .isFalse();
    }

    @Test
    void emailMarketingShouldNotBeRegisteredInSidebar() {

        var sidebarFeatures =
                new MarketingFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .map(item -> item.feature())
                        .toList();

        assertThat(sidebarFeatures)
                .doesNotContain(
                        Feature.EMAIL_MARKETING
                );
    }

    private FeatureDefinition definitionFor(
            Feature feature
    ) {

        return new MarketingFeatures()
                .descriptor()
                .getFeatures()
                .stream()
                .filter(definition ->
                        definition.getFeature() == feature
                )
                .findFirst()
                .orElseThrow();
    }
}
