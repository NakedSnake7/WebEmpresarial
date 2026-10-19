package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarketingPermissionContractTest {

    @Test
    void reviewNavigationMustUseIndependentPermissions() {

        List<SidebarItemDefinition> items =
                new MarketingFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .toList();

        SidebarItemDefinition reviews =
                item(items, "Reseñas");

        SidebarItemDefinition create =
                item(items, "Nueva reseña");

        assertThat(
                reviews.requiredPermission()
        ).isEqualTo(
                AdminPermission.REVIEW_VIEW
        );

        assertThat(reviews.url())
                .isEqualTo("/resenas");

        assertThat(
                create.requiredPermission()
        ).isEqualTo(
                AdminPermission.REVIEW_CREATE
        );

        assertThat(create.url())
                .isEqualTo("/resenas/nueva");
    }

    @Test
    void reviewDashboardWidgetMustRequireReviewView() {

        FeatureDefinition reviews =
                new MarketingFeatures()
                        .descriptor()
                        .getFeatures()
                        .stream()
                        .filter(definition ->
                                definition.getFeature()
                                        == Feature.REVIEWS
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(reviews.getUrl())
                .isEqualTo("/resenas");

        assertThat(
                reviews.getDashboardPermission()
        ).isEqualTo(
                AdminPermission.REVIEW_VIEW
        );
    }

    private SidebarItemDefinition item(
            List<SidebarItemDefinition> items,
            String label
    ) {

        return items.stream()
                .filter(item ->
                        label.equals(item.title())
                )
                .findFirst()
                .orElseThrow();
    }
}
