package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BillingFeaturesPermissionContractTest {

    @Test
    void stripeConnectDashboardShouldRequireExplicitPermission() {

        FeatureDefinition stripeConnect =
                new BillingFeatures()
                        .descriptor()
                        .getFeatures()
                        .stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.STRIPE_CONNECT
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(
                stripeConnect
                        .getAccessPolicy()
                        .getMinimumPlan()
        ).isEqualTo(StorePlan.PRO);

        assertThat(
                stripeConnect.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.STRIPE_CONNECT_MANAGE
        );

        assertThat(
                stripeConnect
                        .getPresentation()
                        .isShowInDashboard()
        ).isTrue();

        assertThat(
                stripeConnect
                        .getPresentation()
                        .isShowInSidebar()
        ).isFalse();
    }

    @Test
    void stripeConnectShouldNotBeRegisteredInSidebar() {

        var sidebarFeatures =
                new BillingFeatures()
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
                        Feature.STRIPE_CONNECT
                );
    }
}
