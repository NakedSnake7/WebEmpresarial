package com.webempresarial.store.service;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class ProvisioningOnboardingE2EIT {

    @Autowired
    private ProvisioningService provisioningService;

    @Test
    void shouldProvisionRealTenantAndSendActivationEmail()
            throws Exception {

        String recipient =
                System.getenv(
                        "WEBEMPRESARIAL_TEST_EMAIL"
                );

        if (recipient == null
                || recipient.isBlank()) {

            throw new IllegalStateException(
                    "Define WEBEMPRESARIAL_TEST_EMAIL "
                    + "con un correo controlado"
            );
        }

        String suffix =
                String.valueOf(
                        System.currentTimeMillis()
                );

        String domain =
                "onboarding-e2e-" + suffix;

        Store store =
                provisioningService
                        .provisionStoreFromCheckout(
                                "WebEmpresarial E2E",
                                domain,
                                "Jovani E2E",
                                recipient,
                                StorePlan.BASIC,
                                "cus_e2e_" + suffix,
                                "sub_e2e_" + suffix,
                                "price_e2e_basic"
                        );

        assertThat(store)
                .isNotNull();

        assertThat(store.getId())
                .isNotNull();

        assertThat(store.getNombre())
                .isEqualTo(
                        "WebEmpresarial E2E"
                );

        assertThat(store.getDominio())
                .isEqualTo(
                        domain
                        + ".web-empresarial.com"
                );

        assertThat(store.isActiva())
                .isTrue();

        assertThat(store.getPlan())
                .isEqualTo(
                        StorePlan.BASIC
                );

        System.out.println(
                "\n======================================"
        );

        System.out.println(
                "TENANT E2E CREADO"
        );

        System.out.println(
                "Store ID: "
                + store.getId()
        );

        System.out.println(
                "Dominio: "
                + store.getDominio()
        );

        System.out.println(
                "Email: "
                + recipient
        );

        System.out.println(
                "======================================\n"
        );
    }
}