package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlanFeatureMatrixContractTest {

    private static final Set<Feature> BASIC_FEATURES =
            EnumSet.of(
                    Feature.PRODUCTS,
                    Feature.CATEGORIES,
                    Feature.INVENTORY,
                    Feature.INVENTORY_KARDEX,
                    Feature.ORDERS,
                    Feature.CHECKOUT,
                    Feature.REVIEWS
            );

    private static final Set<Feature> PRO_FEATURES =
            EnumSet.of(
                    Feature.COUPONS,

                    Feature.CRM,
                    Feature.LEADS,
                    Feature.PIPELINE,
                    Feature.TASKS,
                    Feature.PROPOSALS,

                    Feature.STRIPE_CONNECT,

                    Feature.ANALYTICS,
                    Feature.CUSTOM_DOMAIN,
                    Feature.MULTI_USER
            );

    private static final Set<Feature> PREMIUM_FEATURES =
            EnumSet.of(
                    Feature.EMAIL_MARKETING,
                    Feature.WHATSAPP_AUTOMATION,
                    Feature.AUTOMATIONS,

                    Feature.WHITE_LABEL_FULL,
                    Feature.API_ACCESS
            );

    @Test
    void everyFeatureMustBeRegisteredExactlyOnce() {

        Map<Feature, FeatureDefinition> definitions =
                definitions();

        assertThat(definitions.keySet())
                .containsExactlyInAnyOrder(
                        Feature.values()
                );
    }

    @Test
    void basicFeaturesMustRequireBasicPlan() {

        assertMinimumPlan(
                BASIC_FEATURES,
                StorePlan.BASIC
        );
    }

    @Test
    void proFeaturesMustRequireProPlan() {

        assertMinimumPlan(
                PRO_FEATURES,
                StorePlan.PRO
        );
    }

    @Test
    void premiumFeaturesMustRequirePremiumPlan() {

        assertMinimumPlan(
                PREMIUM_FEATURES,
                StorePlan.PREMIUM
        );
    }

    @Test
    void basicPlanShouldExposeOnlyBasicFeatures() {

        assertThat(
                availableFor(StorePlan.BASIC)
        ).containsExactlyInAnyOrderElementsOf(
                BASIC_FEATURES
        );
    }

    @Test
    void proPlanShouldExposeBasicAndProFeatures() {

        Set<Feature> expected =
                EnumSet.copyOf(
                        BASIC_FEATURES
                );

        expected.addAll(
                PRO_FEATURES
        );

        assertThat(
                availableFor(StorePlan.PRO)
        ).containsExactlyInAnyOrderElementsOf(
                expected
        );
    }

    @Test
    void premiumPlanShouldExposeEveryFeature() {

        assertThat(
                availableFor(StorePlan.PREMIUM)
        ).containsExactlyInAnyOrder(
                Feature.values()
        );
    }

    @Test
    void planTiersMustBeStrictlyCumulative() {

        Set<Feature> basic =
                availableFor(
                        StorePlan.BASIC
                );

        Set<Feature> pro =
                availableFor(
                        StorePlan.PRO
                );

        Set<Feature> premium =
                availableFor(
                        StorePlan.PREMIUM
                );

        assertThat(pro)
                .containsAll(basic);

        assertThat(premium)
                .containsAll(pro);

        assertThat(pro)
                .hasSizeGreaterThan(
                        basic.size()
                );

        assertThat(premium)
                .hasSizeGreaterThan(
                        pro.size()
                );
    }

    @Test
    void featureGroupsMustNotOverlap() {

        assertThat(BASIC_FEATURES)
                .doesNotContainAnyElementsOf(
                        PRO_FEATURES
                )
                .doesNotContainAnyElementsOf(
                        PREMIUM_FEATURES
                );

        assertThat(PRO_FEATURES)
                .doesNotContainAnyElementsOf(
                        PREMIUM_FEATURES
                );
    }

    @Test
    void navigationFeaturesMustHaveRoutableUrl() {

        definitions()
                .values()
                .stream()
                .filter(definition ->
                        definition
                                .getPresentation()
                                .isShowInSidebar()
                        ||
                        definition
                                .getPresentation()
                                .isShowInDashboard()
                )
                .forEach(definition -> {

                    assertThat(definition.getUrl())
                            .as(
                                    "%s visible en navegación "
                                            + "debe tener URL real",
                                    definition.getFeature()
                            )
                            .isNotNull()
                            .isNotBlank()
                            .isNotEqualTo("#")
                            .startsWith("/");
                });
    }

    @Test
    void dashboardFeaturesMustDeclarePermission() {

        definitions()
                .values()
                .stream()
                .filter(definition ->
                        definition
                                .getPresentation()
                                .isShowInDashboard()
                )
                .forEach(definition ->

                        assertThat(
                                definition
                                        .getDashboardPermission()
                        )
                        .as(
                                "%s visible en dashboard "
                                        + "debe declarar permiso",
                                definition.getFeature()
                        )
                        .isNotNull()
                );
    }

    private void assertMinimumPlan(
            Set<Feature> features,
            StorePlan expectedPlan
    ) {

        Map<Feature, FeatureDefinition> definitions =
                definitions();

        for (Feature feature : features) {

            assertThat(
                    definitions
                            .get(feature)
                            .getAccessPolicy()
                            .getMinimumPlan()
            )
            .as(
                    "%s debe requerir %s",
                    feature,
                    expectedPlan
            )
            .isEqualTo(
                    expectedPlan
            );
        }
    }

    private Set<Feature> availableFor(
            StorePlan plan
    ) {

        Set<Feature> result =
                EnumSet.noneOf(
                        Feature.class
                );

        definitions()
                .forEach(
                        (feature, definition) -> {

                            if (definition
                                    .getAccessPolicy()
                                    .isAvailableFor(plan)) {

                                result.add(
                                        feature
                                );
                            }
                        }
                );

        return result;
    }

    private Map<Feature, FeatureDefinition> definitions() {

        Map<Feature, FeatureDefinition> definitions =
                new EnumMap<>(
                        Feature.class
                );

        for (PlatformModule module : modules()) {

            for (FeatureDefinition definition :
                    module
                            .descriptor()
                            .getFeatures()) {

                Feature feature =
                        definition.getFeature();

                FeatureDefinition previous =
                        definitions.put(
                                feature,
                                definition
                        );

                assertThat(previous)
                        .as(
                                "Feature duplicada: %s",
                                feature
                        )
                        .isNull();
            }
        }

        return definitions;
    }

    private List<PlatformModule> modules() {

        return List.of(
                new EcommerceFeatures(),
                new CrmFeatures(),
                new MarketingFeatures(),
                new BillingFeatures(),
                new AutomationFeatures(),
                new PlatformFeatures()
        );
    }
}
