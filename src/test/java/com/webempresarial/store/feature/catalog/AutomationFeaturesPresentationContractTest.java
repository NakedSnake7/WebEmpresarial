package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutomationFeaturesPresentationContractTest {

    @Test
    void automationsShouldRemainPremiumButHiddenUntilImplemented() {

        FeatureDefinition definition =
                definitionFor(
                        Feature.AUTOMATIONS
                );

        assertThat(
                definition
                        .getAccessPolicy()
                        .getMinimumPlan()
        ).isEqualTo(
                StorePlan.PREMIUM
        );

        FeaturePresentation presentation =
                definition.getPresentation();

        assertThat(
                presentation.isShowInSidebar()
        ).isFalse();

        assertThat(
                presentation.isShowInDashboard()
        ).isFalse();

        assertThat(
                presentation.isShowInBilling()
        ).isTrue();

        assertThat(
                presentation.isShowUpgradeCard()
        ).isTrue();

        assertThat(
                presentation.isTrackUsage()
        ).isFalse();
    }

    @Test
    void whatsappAutomationShouldRemainPremiumButHiddenUntilImplemented() {

        FeatureDefinition definition =
                definitionFor(
                        Feature.WHATSAPP_AUTOMATION
                );

        assertThat(
                definition
                        .getAccessPolicy()
                        .getMinimumPlan()
        ).isEqualTo(
                StorePlan.PREMIUM
        );

        FeaturePresentation presentation =
                definition.getPresentation();

        assertThat(
                presentation.isShowInSidebar()
        ).isFalse();

        assertThat(
                presentation.isShowInDashboard()
        ).isFalse();

        assertThat(
                presentation.isShowInBilling()
        ).isTrue();

        assertThat(
                presentation.isShowUpgradeCard()
        ).isTrue();

        assertThat(
                presentation.isTrackUsage()
        ).isFalse();
    }

    @Test
    void unimplementedAutomationFeaturesShouldNotBeRegisteredInSidebar() {

        var sidebarFeatures =
                new AutomationFeatures()
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
                        Feature.WHATSAPP_AUTOMATION,
                        Feature.AUTOMATIONS
                );
    }

    private FeatureDefinition definitionFor(
            Feature feature
    ) {

        return new AutomationFeatures()
                .descriptor()
                .getFeatures()
                .stream()
                .filter(definition ->
                        definition.getFeature() == feature
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Feature no registrada: " + feature
                        )
                );
    }
}
