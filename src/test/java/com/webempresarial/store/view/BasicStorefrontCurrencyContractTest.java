package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BasicStorefrontCurrencyContractTest {

    private static final Path BASIC_INDEX =
            Path.of(
                    "src/main/resources/templates/themes/basic/"
                    + "index.html"
            );

    @Test
    void productPriceMustUseStoreCurrencyInsteadOfJvmLocale()
            throws Exception {

        String template =
                Files.readString(BASIC_INDEX);

        assertThat(template)
                .contains("#numbers.formatDecimal(")
                .contains("settings.currency")
                .contains("'MXN'")
                .doesNotContain(
                        "#numbers.formatCurrency("
                );
    }
}
