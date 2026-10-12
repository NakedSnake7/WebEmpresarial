package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TransferPaymentEmailTemplateTest {

    private static final Path TEMPLATE =
            Path.of(
                    "src/main/resources/email/"
                    + "email-transferencia.html"
            );

    @Test
    void transferEmailTemplateMustExist() {

        assertThat(TEMPLATE)
                .as(
                        "Debe existir el template de instrucciones SPEI"
                )
                .exists()
                .isRegularFile();
    }

    @Test
    void transferEmailMustExposeStoreBankingTokens()
            throws Exception {

        assertThat(TEMPLATE)
                .exists();

        String html =
                Files.readString(TEMPLATE);

        assertThat(html)
                .contains("{NOMBRE}")
                .contains("{NUMERO_ORDEN}")
                .contains("{LISTADO_PRODUCTOS}")
                .contains("{SUBTOTAL}")
                .contains("{ENVIO}")
                .contains("{TOTAL}")
                .contains("{BANCO}")
                .contains("{TITULAR}")
                .contains("{CLABE}");
    }

    @Test
    void transferEmailMustExplainPaymentIsPending()
            throws Exception {

        assertThat(TEMPLATE)
                .exists();

        String html =
                Files.readString(TEMPLATE);

        assertThat(html)
                .containsIgnoringCase(
                        "pendiente"
                )
                .containsIgnoringCase(
                        "transferencia"
                );
    }
}
