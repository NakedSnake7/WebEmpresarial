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
    void shouldExposeStripeAccordingToStoreConnection(
            boolean stripeConnected
    ) {
        for (String theme : THEMES) {
            Store store = new Store();
            store.setStripeConnected(stripeConnected);

            String html = renderCheckout(
                    theme,
                    store
            );

            assertThat(html)
                    .as(
                            "theme=%s stripeConnected=%s",
                            theme,
                            stripeConnected
                    )
                    .contains("id=\"payStripe\"")
                    .contains("value=\"STRIPE\"");

            if (stripeConnected) {
                assertThat(html)
                        .as("Stripe enabled for theme=%s", theme)
                        .doesNotContain(
                                "disabled=\"disabled\""
                        );
            } else {
                assertThat(html)
                        .as("Stripe disabled for theme=%s", theme)
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
