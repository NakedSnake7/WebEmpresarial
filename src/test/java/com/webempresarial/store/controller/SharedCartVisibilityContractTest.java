package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SharedCartVisibilityContractTest {

    private static final Path SHARED_CSS =
            Path.of(
                    "src/main/resources/static/assets/css/public/"
                    + "customer.css"
            );

    private static final Path CART_JS =
            Path.of(
                    "src/main/resources/static/assets/js/shared/cart/"
                    + "carrito.js"
            );

    @Test
    void sharedCartMustBeClosedUntilOpenClassIsPresent()
            throws Exception {

        String css =
                Files.readString(SHARED_CSS);

        assertThat(css)
                .contains(".cart-dropdown:not(.open)")
                .contains("display: none;");
    }

    @Test
    void cartButtonMustControlOpenState()
            throws Exception {

        String javascript =
                Files.readString(CART_JS);

        assertThat(javascript)
                .contains(
                        "cartDropdown.classList.toggle('open')"
                )
                .contains(
                        "cartDropdown?.classList.remove(\"open\")"
                );
    }
}
