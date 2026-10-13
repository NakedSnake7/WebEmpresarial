package com.webempresarial.store.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class StorefrontCheckoutReturnSecurityContractTest {

    private static final Path SECURITY_CONFIG =
            Path.of(
                    "src/main/java/com/webempresarial/store/config/"
                    + "SecurityConfig.java"
            );

    @Test
    void checkoutReturnRoutesMustRemainPublic()
            throws Exception {

        String source =
                Files.readString(SECURITY_CONFIG);

        Pattern publicMatchers =
                Pattern.compile(
                        "\\.requestMatchers\\s*\\("
                        + "[\\s\\S]*?\"/gracias\""
                        + "[\\s\\S]*?\"/checkout-cancel\""
                        + "[\\s\\S]*?\\)"
                        + "\\s*\\.permitAll\\(\\)",
                        Pattern.MULTILINE
                );

        assertThat(publicMatchers.matcher(source).find())
                .as(
                        "/gracias y /checkout-cancel deben "
                        + "permanecer en un bloque permitAll()"
                )
                .isTrue();
    }
}
