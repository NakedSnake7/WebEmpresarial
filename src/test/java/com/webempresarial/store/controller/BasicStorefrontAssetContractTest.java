package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BasicStorefrontAssetContractTest {

    private static final Path BASIC_CSS =
            Path.of(
                    "src/main/resources/static/"
                    + "themes/basic/assets/css/style.css"
            );

    @Test
    void basicStorefrontMustProvideItsStylesheet()
            throws Exception {

        assertThat(BASIC_CSS)
                .exists()
                .isRegularFile();

        String css =
                Files.readString(BASIC_CSS);

        assertThat(css)
                .isNotBlank()
                .contains(".basic-hero")
                .contains(".basic-product-grid")
                .contains(".basic-btn-primary")
                .contains("var(--store-primary)")
                .contains("var(--store-accent)");
    }
}
