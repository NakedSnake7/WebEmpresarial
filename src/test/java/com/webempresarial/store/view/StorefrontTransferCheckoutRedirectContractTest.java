package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class StorefrontTransferCheckoutRedirectContractTest {

    private static final Path CART_JS =
            Path.of(
                    "src/main/resources/static/assets/js/"
                    + "shared/cart/carrito.js"
            );

    @Test
    void transferCheckoutMustRedirectToOrderConfirmation()
            throws IOException {

        assertThat(CART_JS)
                .exists()
                .isRegularFile();

        String js =
                Files.readString(CART_JS);

        Pattern transferFlow =
                Pattern.compile(
                        """
                        (?s)
                        if\\s*\\(
                            paymentMethod\\s*===\\s*["']TRANSFER["']
                        \\)
                        \\s*\\{
                        .*?
                        /api/checkout
                        .*?
                        data\\s*=\\s*JSON\\.parse\\(text\\)
                        .*?
                        data\\.orderId
                        .*?
                        /gracias\\?order_id=
                        """,
                        Pattern.COMMENTS
                );

        assertThat(
                transferFlow.matcher(js).find()
        )
                .as(
                        "TRANSFER debe redirigir a "
                        + "/gracias?order_id=<orderId>"
                )
                .isTrue();
    }

    @Test
    void transferRedirectMustUseServerOrderId()
            throws IOException {

        String js =
                Files.readString(CART_JS);

        assertThat(js)
                .contains("data.orderId")
                .contains("/gracias?order_id=");
    }
}
