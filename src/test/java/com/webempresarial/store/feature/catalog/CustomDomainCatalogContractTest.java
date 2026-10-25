package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomDomainCatalogContractTest {

    @Test
    void customDomainMustBeImplementedAsProFeature() {

        FeatureDefinition definition =
                customDomain();

        FeaturePresentation presentation =
                definition.getPresentation();

        assertThat(
                definition.getUrl()
        ).isEqualTo(
                "/admin/domains"
        );

        assertThat(
                definition
                        .getAccessPolicy()
                        .getMinimumPlan()
        ).isEqualTo(
                StorePlan.PRO
        );

        assertThat(
                presentation.isShowInSidebar()
        ).isTrue();

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
    void customDomainMustHavePermissionAwareSidebarItem() {

        List<SidebarItemDefinition> matches =
                new PlatformFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section
                                        .getItems()
                                        .stream()
                        )
                        .filter(item ->
                                item.feature()
                                        == Feature.CUSTOM_DOMAIN
                        )
                        .toList();

        assertThat(matches)
                .hasSize(1);

        SidebarItemDefinition item =
                matches.get(0);

        assertThat(item.title())
                .isEqualTo("Dominio");

        assertThat(item.url())
                .isEqualTo(
                        "/admin/domains"
                );

        assertThat(
                item.requiredPermission()
        ).isEqualTo(
                AdminPermission.DOMAIN_VIEW
        );
    }

    private FeatureDefinition customDomain() {

        return new PlatformFeatures()
                .descriptor()
                .getFeatures()
                .stream()
                .filter(definition ->
                        definition.getFeature()
                                == Feature.CUSTOM_DOMAIN
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Feature.CUSTOM_DOMAIN "
                                + "no está registrada"
                        )
                );
    }
}
