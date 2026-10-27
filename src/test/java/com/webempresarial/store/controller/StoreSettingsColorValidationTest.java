package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StoreSettingsColorValidationTest {

    @Test
    void validHexColorMustBeAccepted() {

        assertThat(
                StoreSettingsController.validatedHexColor(
                        "#7b1827",
                        "#111827",
                        "#111827"
                )
        ).isEqualTo("#7b1827");
    }

    @Test
    void surroundingWhitespaceMustBeNormalized() {

        assertThat(
                StoreSettingsController.validatedHexColor(
                        "  #A1B2C3  ",
                        "#111827",
                        "#111827"
                )
        ).isEqualTo("#A1B2C3");
    }

    @Test
    void invalidColorMustPreserveCurrentSafeValue() {

        assertThat(
                StoreSettingsController.validatedHexColor(
                        "red; } body { display:none",
                        "#7b1827",
                        "#111827"
                )
        ).isEqualTo("#7b1827");
    }

    @Test
    void invalidCandidateAndCurrentMustUseFallback() {

        assertThat(
                StoreSettingsController.validatedHexColor(
                        "</style><script>alert(1)</script>",
                        "not-a-color",
                        "#111827"
                )
        ).isEqualTo("#111827");
    }
}
