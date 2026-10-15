package com.webempresarial.store.controller.admin;

import com.webempresarial.store.controller.advice.FeatureAccessExceptionHandler;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureAccessExceptionHandlerTest {

    private final FeatureAccessExceptionHandler handler =
            new FeatureAccessExceptionHandler();

    @Test
    void lockedMultiUserShouldRedirectToUpgrade() {

        RedirectAttributesModelMap attributes =
                new RedirectAttributesModelMap();

        String result =
                handler.handleFeatureLocked(
                        new FeatureLockedException(
                                Feature.MULTI_USER
                        ),
                        attributes
                );

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/upgrade?feature=MULTI_USER"
                );

        assertThat(
                attributes
                        .getFlashAttributes()
                        .get("lockedFeature")
        )
        .isEqualTo(
                "MULTI_USER"
        );
    }
}
