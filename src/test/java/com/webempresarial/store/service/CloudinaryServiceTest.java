package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryServiceTest {

    @Test
    void shouldExtractPublicIdWithoutCloudinaryVersion() {
        CloudinaryService service =
                new CloudinaryService(
                        "demo",
                        "api-key",
                        "api-secret"
                );

        String url =
                "https://res.cloudinary.com/demo/"
                        + "image/upload/v1234567890/"
                        + "webempresarial/stores/100/"
                        + "branding/logo.webp";

        String publicId =
                service.extraerPublicIdDesdeUrl(url);

        assertThat(publicId)
                .isEqualTo(
                        "webempresarial/stores/100/"
                                + "branding/logo"
                );
    }
}
