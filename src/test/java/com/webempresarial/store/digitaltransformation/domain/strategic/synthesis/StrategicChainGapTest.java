package com.webempresarial.store.digitaltransformation.domain.strategic.synthesis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategicChainGapTest {

    @Test
    void shouldNormalizeDescription() {
        StrategicChainGap gap =
                new StrategicChainGap(
                        StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                        "  Falta el objetivo empresarial  "
                );

        assertThat(gap.description())
                .isEqualTo(
                        "Falta el objetivo empresarial"
                );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() ->
                new StrategicChainGap(
                        StrategicChainGapType.MISSING_BUSINESS_OBJECTIVE,
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "descripción"
                );
    }
}