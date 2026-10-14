package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StorefrontStripeConfirmationContractTest {

    private static final Path CONFIRMATION =
            Path.of(
                    "src/main/resources/templates/themes/shared/"
                            + "fragments/order-confirmation.html"
            );

    @Test
    void stripeConfirmationMustDistinguishPaidFromPending()
            throws IOException {

        String html = Files.readString(CONFIRMATION);

        assertThat(html)
                .contains(
                        "order.paymentMethod.name() == 'STRIPE'"
                );

        assertThat(html)
                .contains("th:if=\"${order.paid}\"");

        assertThat(html)
                .contains("th:unless=\"${order.paid}\"");

        assertThat(html)
                .contains("Pago confirmado");

        assertThat(html)
                .contains(
                        "Stripe confirmó correctamente tu pago."
                );

        assertThat(html)
                .contains("Pedido registrado");

        assertThat(html)
                .contains(
                        "actualizará cuando Stripe confirme el pago."
                );
    }
}
