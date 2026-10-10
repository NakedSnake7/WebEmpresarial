package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class StorefrontShippingPolicyContractTest {

    private static final Path LAYOUT =
            Path.of(
                    "src/main/resources/templates/layout/main.html"
            );

    private static final List<Path> CARTS = List.of(
            Path.of(
                    "src/main/resources/static/assets/js/shared/cart/carrito.js"
            ),
            Path.of(
                    "src/main/resources/static/themes/barleypunch/assets/js/cart/carrito.js"
            ),
            Path.of(
                    "src/main/resources/static/themes/stride/assets/js/cart/carrito.js"
            )
    );

    @Test
    void layoutShouldExposeStoreShippingPolicyToJavascript()
            throws Exception {

        String html = Files.readString(LAYOUT);

        assertThat(html)
                .contains("window.storeCommerceConfig")
                .contains("freeShippingEnabled")
                .contains("freeShippingThreshold");
    }

    @Test
    void runtimeCartsShouldUseStoreShippingPolicy()
            throws Exception {

        for (Path cart : CARTS) {
            String javascript = Files.readString(cart);

            assertThat(javascript)
                    .as(cart.toString())
                    .contains("window.storeCommerceConfig")
                    .contains("FREE_SHIPPING_ENABLED")
                    .contains("LIMITE_ENVIO_GRATIS_EFECTIVO")
                    .contains("mostrarEnvioGratis")
                    .doesNotContain(
                            "const LIMITE_ENVIO_GRATIS = 1250"
                    );
        }
    }
}
