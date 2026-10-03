package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

class SaaSCheckoutControllerTest {

    private final SaaSCheckoutController controller =
            new SaaSCheckoutController();

    @Test
    void shouldRenderSuccessPageWithSessionId() {

        Model model = new ConcurrentModel();

        String view = controller.success(
                "cs_test_123456",
                model
        );

        assertThat(view)
                .isEqualTo("billing/success");

        assertThat(model.getAttribute("sessionId"))
                .isEqualTo("cs_test_123456");
    }

    @Test
    void shouldRenderSuccessPageWithoutSessionId() {

        Model model = new ConcurrentModel();

        String view = controller.success(
                null,
                model
        );

        assertThat(view)
                .isEqualTo("billing/success");

        assertThat(model.getAttribute("sessionId"))
                .isNull();
    }
}