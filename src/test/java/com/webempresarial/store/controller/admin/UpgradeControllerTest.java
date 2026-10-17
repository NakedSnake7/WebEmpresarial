package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeControllerTest {

    private final UpgradeController controller =
            new UpgradeController();

    @Test
    void multiUserShouldRequireProPlan() {

        Model model = new ExtendedModelMap();

        String view =
                controller.upgrade(
                        Feature.MULTI_USER,
                        model
                );

        assertThat(view)
                .isEqualTo("admin/upgrade/index");

        assertThat(model.getAttribute("feature"))
                .isEqualTo(Feature.MULTI_USER);

        assertThat(model.getAttribute("requiredPlan"))
                .isEqualTo(StorePlan.PRO);
    }

    @Test
    void premiumFeatureShouldRequirePremiumPlan() {

        Model model = new ExtendedModelMap();

        controller.upgrade(
                Feature.API_ACCESS,
                model
        );

        assertThat(model.getAttribute("requiredPlan"))
                .isEqualTo(StorePlan.PREMIUM);
    }

    @Test
    void basicFeatureShouldRequireBasicPlan() {

        Model model = new ExtendedModelMap();

        controller.upgrade(
                Feature.PRODUCTS,
                model
        );

        assertThat(model.getAttribute("requiredPlan"))
                .isEqualTo(StorePlan.BASIC);
    }

    @Test
    void missingFeatureShouldDefaultToProPlan() {

        Model model = new ExtendedModelMap();

        controller.upgrade(
                null,
                model
        );

        assertThat(model.getAttribute("requiredPlan"))
                .isEqualTo(StorePlan.PRO);
    }
}
