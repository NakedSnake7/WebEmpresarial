package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class StorefrontPurchaseFlowContractTest {

    private static final Path BASIC_INDEX =
            Path.of(
                    "src/main/resources/templates/themes/basic/"
                    + "index.html"
            );

    private static final Path SHARED_DETAIL =
            Path.of(
                    "src/main/resources/templates/themes/shared/"
                    + "producto-detalle.html"
            );

    @Test
    void basicCardsMustSeparateSimpleAndVariantProducts()
            throws Exception {

        String source =
                Files.readString(BASIC_INDEX);

        assertThat(source)
                .contains(
                        "!producto.tieneVariantes"
                )
                .contains(
                        "producto.tieneVariantes"
                )
                .contains(
                        "add-to-cart"
                )
                .contains(
                        "data-product-id"
                )
                .contains(
                        "data-quantity-id"
                )
                .contains(
                        "data-original-stock"
                )
                .contains(
                        "/producto-detalle/{id}"
                )
                .contains(
                        "Ver opciones"
                )
                .doesNotContain(
                        "basic-add-cart"
                );
    }

    @Test
    void sharedDetailMustRequireVariantSelection()
            throws Exception {

        String source =
                Files.readString(SHARED_DETAIL);

        assertThat(source)
                .contains(
                        "selectedVariant = null"
                )
                .contains(
                        "variant?.atributos"
                )
                .contains(
                        "Object.entries("
                )
                .contains(
                        "varianteId:"
                )
                .contains(
                        "window.cartStore.add"
                )
                .contains(
                        "originalStock:"
                )
                .contains(
                        "Selecciona una opción"
                );
    }

    @Test
    void saasThemesMustProvideProductDetailView()
            throws Exception {

        for (String theme :
                new String[] {
                        "basic",
                        "pro",
                        "premium"
                }) {

            Path template =
                    Path.of(
                            "src/main/resources/templates/themes/"
                            + theme
                            + "/producto-detalle.html"
                    );

            assertThat(template)
                    .exists();

            assertThat(
                    Files.readString(template)
            )
                    .contains(
                            "themes/shared/producto-detalle"
                    );
        }
    }

    @Test
    void checkoutFieldsMustProvideValidationFeedback()
            throws Exception {

        Path checkout =
                Path.of(
                        "src/main/resources/templates/themes/shared/"
                        + "fragments/checkout.html"
                );

        Path cart =
                Path.of(
                        "src/main/resources/static/assets/js/shared/"
                        + "cart/carrito.js"
                );

        String checkoutSource =
                Files.readString(checkout);

        String cartSource =
                Files.readString(cart);

        assertThat(checkoutSource)
                .contains("id=\"fullName\"")
                .contains("id=\"email\"")
                .contains("id=\"phone\"")
                .contains("id=\"address\"");

        assertThat(
                checkoutSource
                    .split("invalid-feedback", -1)
                    .length - 1
        )
                .isGreaterThanOrEqualTo(4);

        assertThat(cartSource)
                .contains("if (errorDiv)")
                .contains("return valido;");
    }



    @Test
    void proAndPremiumCardsMustUseSharedPurchaseContract()
            throws Exception {

        String pro =
                Files.readString(
                        Path.of(
                                "src/main/resources/templates/themes/"
                                + "pro/index.html"
                        )
                );

        String premium =
                Files.readString(
                        Path.of(
                                "src/main/resources/templates/themes/"
                                + "premium/index.html"
                        )
                );

        assertThat(pro)
                .contains("!producto.tieneVariantes")
                .contains("producto.tieneVariantes")
                .contains("add-to-cart")
                .contains("data-product-id")
                .contains("data-name")
                .contains("data-price")
                .contains("data-quantity-id")
                .contains("data-original-stock")
                .contains("/producto-detalle/{id}")
                .contains("Ver opciones")
                .doesNotContain("pro-add-cart");

        assertThat(
                pro.split(
                        Pattern.quote(
                                "/producto-detalle/{id}"
                        ),
                        -1
                ).length - 1
        )
                .isGreaterThanOrEqualTo(1);

        assertThat(premium)
                .contains("!featured.tieneVariantes")
                .contains("featured.tieneVariantes")
                .contains("!producto.tieneVariantes")
                .contains("producto.tieneVariantes")
                .contains("add-to-cart")
                .contains("data-product-id")
                .contains("data-name")
                .contains("data-price")
                .contains("data-quantity-id")
                .contains("data-original-stock")
                .contains("/producto-detalle/{id}")
                .contains("Ver opciones")
                .doesNotContain("premium-add-cart");

        assertThat(
                premium.split(
                        Pattern.quote(
                                "/producto-detalle/{id}"
                        ),
                        -1
                ).length - 1
        )
                .isGreaterThanOrEqualTo(2);

        assertThat(
                premium.split(
                        "add-to-cart",
                        -1
                ).length - 1
        )
                .isGreaterThanOrEqualTo(2);
    }



    @Test
    void saasThemesMustProvideStylesheet()
            throws Exception {

        for (String theme :
                new String[] {
                        "basic",
                        "pro",
                        "premium"
                }) {

            Path stylesheet =
                    Path.of(
                            "src/main/resources/static/themes/"
                            + theme
                            + "/assets/css/style.css"
                    );

            assertThat(stylesheet)
                    .exists()
                    .isRegularFile();

            assertThat(Files.size(stylesheet))
                    .isGreaterThan(0L);
        }
    }


}
