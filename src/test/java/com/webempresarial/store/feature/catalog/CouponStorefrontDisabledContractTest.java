package com.webempresarial.store.feature.catalog;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CouponStorefrontDisabledContractTest {

    private static final List<Path> CART_FRAGMENTS =
            List.of(
                    Path.of(
                            "src/main/resources/templates/themes/shared/"
                            + "fragments/cart.html"
                    ),
                    Path.of(
                            "src/main/resources/templates/themes/barleypunch/"
                            + "fragments/cart.html"
                    ),
                    Path.of(
                            "src/main/resources/templates/themes/stride/"
                            + "fragments/cart.html"
                    ),
                    Path.of(
                            "src/main/resources/templates/themes/WebEmpresarial/"
                            + "fragments/cart.html"
                    )
            );

    private static final List<Path> CART_STORES =
            List.of(
                    Path.of(
                            "src/main/resources/static/assets/js/shared/"
                            + "cart/cartStore.js"
                    ),
                    Path.of(
                            "src/main/resources/static/assets/js/shared/"
                            + "components/cartStore.js"
                    ),
                    Path.of(
                            "src/main/resources/static/themes/barleypunch/"
                            + "assets/js/cart/cartStore.js"
                    ),
                    Path.of(
                            "src/main/resources/static/themes/barleypunch/"
                            + "assets/js/components/cartStore.js"
                    ),
                    Path.of(
                            "src/main/resources/static/themes/stride/"
                            + "assets/js/cart/cartStore.js"
                    ),
                    Path.of(
                            "src/main/resources/static/themes/stride/"
                            + "assets/js/components/cartStore.js"
                    )
            );

    @Test
    void couponControlsShouldRemainHiddenUntilServerValidationExists()
            throws Exception {

        for (Path path : CART_FRAGMENTS) {

            String source =
                    Files.readString(path);

            assertThat(source)
                    .as(path.toString())
                    .doesNotContain(
                            "couponInput",
                            "applyCouponBtn",
                            "couponError"
                    );
        }
    }

    @Test
    void persistedCouponsShouldNotBeRestored()
            throws Exception {

        for (Path path : CART_STORES) {

            String source =
                    Files.readString(path);

            assertThat(source)
                    .as(path.toString())
                    .doesNotContain(
                            "coupon = data.coupon || null;"
                    );
        }
    }
}
