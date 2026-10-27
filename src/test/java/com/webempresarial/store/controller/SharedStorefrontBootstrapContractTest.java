package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SharedStorefrontBootstrapContractTest {

    private static final Path SHARED_LAYOUT =
            Path.of(
                    "src/main/resources/templates/"
                    + "themes/shared/layout.html"
            );

    @Test
    void sharedStorefrontMustLoadBootstrapCssBeforeThemeCss()
            throws Exception {

        String layout =
                Files.readString(SHARED_LAYOUT);

        String bootstrap =
                "bootstrap@5.3.3/dist/css/bootstrap.min.css";

        String themeCss =
                "/themes/${theme}/assets/css/style.css";

        assertThat(layout)
                .contains(bootstrap)
                .contains(themeCss)
                .contains(
                        "bootstrap@5.3.3/dist/js/"
                        + "bootstrap.bundle.min.js"
                );

        assertThat(layout.indexOf(bootstrap))
                .isLessThan(
                        layout.indexOf(themeCss)
                );
    }

    @Test
    void brandingHexColorsMustUseTextInlining()
            throws Exception {

        String layout =
                Files.readString(SHARED_LAYOUT);

        assertThat(layout)
                .contains("<style th:inline=\"text\">")
                .contains("--store-primary:")
                .contains("--store-secondary:")
                .contains("--store-accent:")
                .contains("<style th:inline=\"css\">")
                .contains("--store-font:");
    }


}
