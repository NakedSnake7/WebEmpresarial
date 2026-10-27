package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ProductVariantFormStateContractTest {

    private static final Path TEMPLATE =
            Path.of(
                    "src/main/resources/templates/admin/"
                    + "subirProducto.html"
            );

    private static final Path CONTROLLER =
            Path.of(
                    "src/main/java/com/webempresarial/store/controller/"
                    + "ProductoViewController.java"
            );

    @Test
    void unknownCategoryMustNotFallbackToDigitalDownload()
            throws Exception {

        String source =
                Files.readString(TEMPLATE);

        assertThat(source)
                .contains(
                        "return presets[category] || [];"
                )
                .doesNotContain(
                        "return presets[category] || presets.Digital;"
                );
    }

    @Test
    void variantGroupsMustAllowManualOptions()
            throws Exception {

        String source =
                Files.readString(TEMPLATE);

        assertThat(source)
                .contains("variant-option-input")
                .contains("add-variant-option")
                .contains("addManualVariantOption");
    }

    @Test
    void variantsMustBeRestoredAfterValidationError()
            throws Exception {

        String template =
                Files.readString(TEMPLATE);

        String controller =
                Files.readString(CONTROLLER);

        assertThat(template)
                .contains(
                        "th:value=\"${variantesJson != null"
                )
                .contains("restoreVariants()");

        assertThat(controller)
                .contains(
                        "model.addAttribute("
                )
                .contains(
                        "\"variantesJson\","
                );
    }

    @Test
    void variantsMustUseDynamicAttributeNames()
            throws Exception {

        String source =
                Files.readString(TEMPLATE);

        assertThat(source)
                .contains(
                        "[attributeName]:"
                )
                .contains(
                        "row.dataset.size"
                )
                .contains(
                        "Nombre del atributo"
                )
                .doesNotContain(
                        "Color: variantName"
                )
                .doesNotContain(
                        "Talla: row.dataset.size"
                );
    }


}
