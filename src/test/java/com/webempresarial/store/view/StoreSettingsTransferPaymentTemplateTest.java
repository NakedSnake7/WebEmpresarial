package com.webempresarial.store.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StoreSettingsTransferPaymentTemplateTest {

    private static final Path TEMPLATE =
            Path.of(
                    "src/main/resources/templates/admin/store/settings.html"
            );

    @Test
    void shouldExposeStoreSpecificTransferSettingsForm()
            throws Exception {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
                .contains(
                        "th:action=\"@{/admin/store/settings/transfer}\""
                )
                .contains(
                        "name=\"transferEnabled\""
                )
                .contains(
                        "th:checked=\"${settings.transferEnabled}\""
                )
                .contains(
                        "name=\"transferBankName\""
                )
                .contains(
                        "th:value=\"${settings.transferBankName}\""
                )
                .contains(
                        "name=\"transferAccountHolder\""
                )
                .contains(
                        "th:value=\"${settings.transferAccountHolder}\""
                )
                .contains(
                        "name=\"transferClabe\""
                )
                .contains(
                        "th:value=\"${settings.transferClabe}\""
                )
                .contains(
                        "maxlength=\"18\""
                )
                .contains(
                        "pattern=\"[0-9]{18}\""
                );
    }

    @Test
    void shouldExposeTransferSaveFeedback() throws Exception {

        String html = Files.readString(TEMPLATE);

        assertThat(html)
                .contains("param.transferSuccess")
                .contains("param.transferError");
    }
}
