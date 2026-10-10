package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BarleyPunchProductDetailTemplateContractTest {

    private static final Path TEMPLATE = Path.of(
        "src/main/resources/templates/themes/barleypunch/producto-detalle.html"
    );

    @Test
    void shouldUseBarleyPunchSpecificProductLanguage() throws IOException {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
            .contains("bp-product-detail")
            .contains("Volver a las cervezas")
            .contains("Presentación")
            .doesNotContain("Guía de tallas")
            .doesNotContain("Talla —")
            .doesNotContain("124 reseñas");
    }

    @Test
    void shouldPreserveVariantAwareCommerceContract() throws IOException {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
            .contains("producto.variantes")
            .contains("window.cartStore.add")
            .contains("varianteId: varianteSel?.id ?? null")
            .contains("btnCart")
            .contains("btnCartMobile");
    }
}
