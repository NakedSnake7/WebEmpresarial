package com.webempresarial.store.config;

import com.webempresarial.store.model.Store;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

class StorefrontCheckoutStripeTemplateTest {

    private static final String[] THEMES = {
            "WebEmpresarial",
            "barleypunch",
            "stride"
    };

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldExposeStripeAccordingToPaymentAvailability(
            boolean stripeAvailable
    ) {
        for (String theme : THEMES) {

            Store store = new Store();

            store.setStripeConnected(
                    stripeAvailable
            );

            store.setStripeConnectedAccountId(
                    stripeAvailable
                            ? "acct_test_store"
                            : null
            );

            assertThat(
                    store.isStripePaymentAvailable()
            ).isEqualTo(
                    stripeAvailable
            );

            String html = renderCheckout(
                    theme,
                    store
            );

            if (stripeAvailable) {

                assertThat(html)
                        .as(
                                "Stripe disponible para theme=%s",
                                theme
                        )
                        .contains(
                                "id=\"payStripe\""
                        )
                        .contains(
                                "value=\"STRIPE\""
                        )
                        .doesNotContain(
                                "disabled=\"disabled\""
                        );

            } else if (
                    html.contains(
                            "id=\"payStripe\""
                    )
            ) {

                /*
                 * Algunos themes ocultan Stripe cuando no
                 * está disponible y otros muestran la opción
                 * deshabilitada. Ambos comportamientos son
                 * válidos mientras nunca quede seleccionable.
                 */
                assertThat(html)
                        .as(
                                "Stripe no debe quedar habilitado "
                                + "para theme=%s",
                                theme
                        )
                        .contains(
                                "disabled=\"disabled\""
                        );
            }
        }
    }

    private String renderCheckout(
            String theme,
            Store store
    ) {
        ClassLoaderTemplateResolver resolver =
                new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine =
                new SpringTemplateEngine();

        engine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariable("store", store);

        return engine.process(
                "themes/" + theme + "/fragments/checkout",
                context
        );
    }
}
