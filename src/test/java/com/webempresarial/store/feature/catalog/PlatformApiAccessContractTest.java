package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformApiAccessContractTest {


    @Test
    void apiAccessShouldRemainCommercialOnlyUntilImplemented() {

        FeatureDefinition definition =
                apiAccess();

        FeaturePresentation presentation =
                definition.getPresentation();

        assertThat(
                definition.getUrl()
        )
        .isEqualTo("/admin/api");

        assertThat(
                presentation.isShowInSidebar()
        )
        .isFalse();

        assertThat(
                presentation.isShowInDashboard()
        )
        .isFalse();

        assertThat(
                presentation.isShowInBilling()
        )
        .isTrue();

        assertThat(
                presentation.isShowUpgradeCard()
        )
        .isTrue();

        assertThat(
                presentation.isTrackUsage()
        )
        .isFalse();

        assertThat(
                definition.getDashboardPermission()
        )
        .isNull();
    }

    @Test
    void apiAccessShouldNotHaveSidebarRegistrationUntilImplemented() {

        PlatformFeatures platform =
                new PlatformFeatures();

        assertThat(
                platform
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section
                                        .getItems()
                                        .stream()
                        )
                        .map(item -> item.feature())
        )
        .doesNotContain(
                Feature.API_ACCESS
        );
    }

    private FeatureDefinition apiAccess() {

        return new PlatformFeatures()
                .descriptor()
                .getFeatures()
                .stream()
                .filter(definition ->
                        definition.getFeature()
                                == Feature.API_ACCESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Feature.API_ACCESS no está registrada"
                        )
                );
    }
}
