package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.config.MailerSendConfig;
import com.webempresarial.store.entity.StoreSettings;
import com.webempresarial.store.model.Store;

class EmailServiceTransferPaymentTest {

    @Test
    void shouldRenderStoreBankingDataAndPersistedOrderTotal()
            throws Exception {

        MailerSendConfig config =
                mock(MailerSendConfig.class);

        StoreSettingsService storeSettingsService =
                mock(StoreSettingsService.class);

        EmailService service =
                spy(
                        new EmailService(
                                config,
                                storeSettingsService
                        )
                );

        doNothing()
                .when(service)
                .enviarCorreoHTML(
                        anyString(),
                        anyString(),
                        anyString()
                );

        Store store = new Store();
        store.setId(42L);

        StoreSettings settings =
                new StoreSettings();

        settings.setStore(store);
        settings.setTransferEnabled(true);
        settings.setTransferBankName("BBVA");
        settings.setTransferAccountHolder(
                "ACME SA DE CV"
        );
        settings.setTransferClabe(
                "012345678901234567"
        );

        when(storeSettingsService.getOrCreate(store))
                .thenReturn(settings);

        Order order = new Order();

        order.setId(777L);
        order.setStore(store);
        order.setCustomerName("Cliente Test");
        order.setCustomerEmail(
                "cliente@test.com"
        );

        /*
         * Este valor es deliberadamente distinto de la
         * antigua política fija de envío.
         *
         * El correo debe respetar exactamente el total
         * persistido en la orden.
         */
        order.setTotal(
                new BigDecimal("987.65")
        );

        service.enviarCorreoDatosTransferencia(
                order
        );

        ArgumentCaptor<String> html =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(service)
                .enviarCorreoHTML(
                        eq("cliente@test.com"),
                        eq(
                                "💳 Instrucciones de pago - Orden #777"
                        ),
                        html.capture()
                );

        String renderedHtml =
                html.getValue();

        assertThat(renderedHtml)
                .contains("BBVA")
                .contains("ACME SA DE CV")
                .contains("012345678901234567")
                .doesNotContain("{BANCO}")
                .doesNotContain("{TITULAR}")
                .doesNotContain("{CLABE}");

        assertThat(
                renderedHtml.replace(',', '.')
        ).contains("987.65");

        verify(storeSettingsService)
                .getOrCreate(store);
    }
}
