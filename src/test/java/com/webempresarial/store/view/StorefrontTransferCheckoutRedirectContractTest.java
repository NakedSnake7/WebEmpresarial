package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class StorefrontTransferCheckoutRedirectContractTest {

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
    void transferCheckoutMustRedirectToOrderConfirmation()
            throws Exception {

        for (Path cart : CARTS) {

            String javascript =
                    Files.readString(cart);

            assertThat(javascript)
                    .as(cart.toString())
                    .contains("data.orderId")
                    .contains("/gracias?order_id=");
        }
    }

    @Test
    void transferRedirectMustUseServerOrderId()
            throws Exception {

        for (Path cart : CARTS) {

            String javascript =
                    Files.readString(cart);

            assertThat(javascript)
                    .as(cart.toString())
                    .contains(
                            "encodeURIComponent(data.orderId)"
                    );
        }
    }
}
