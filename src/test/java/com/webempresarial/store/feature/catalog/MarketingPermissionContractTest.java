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
    void reviewNavigationShouldExposeSingleReviewsWorkspace() {

        List<SidebarItemDefinition> items =
                new MarketingFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .toList();

        List<SidebarItemDefinition> reviewItems =
                items.stream()
                        .filter(item ->
                                item.feature()
                                        == Feature.REVIEWS
                        )
                        .toList();

        assertThat(reviewItems)
                .singleElement()
                .satisfies(reviews -> {

                    assertThat(reviews.title())
                            .isEqualTo("Reseñas");

                    assertThat(
                            reviews.requiredPermission()
                    ).isEqualTo(
                            AdminPermission.REVIEW_VIEW
                    );

                    assertThat(reviews.url())
                            .isEqualTo("/resenas");
                });

        assertThat(items)
                .noneMatch(item ->
                        "/resenas/nueva".equals(
                                item.url()
                        )
                );
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
}
