package com.webempresarial.store.feature.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformFeaturesPlanContractTest {

    @Test
    void multiUserShouldRequireProPlan()
            throws IOException {

        String source = readSource();

        int start = source.indexOf(
                "private FeatureDefinition multiUser()"
        );

        int end = source.indexOf(
                "private FeatureDefinition apiAccess()",
                start
        );

        assertThat(start)
                .isGreaterThanOrEqualTo(0);

        assertThat(end)
                .isGreaterThan(start);

        String method =
                source.substring(start, end);

        assertThat(method)
                .contains(
                        ".accessPolicy(pro())"
                );

        assertThat(method)
                .doesNotContain(
                        ".accessPolicy(premium())"
                );
    }

    @Test
    void multiUserSidebarShouldAllowOwnerAndAdmin()
            throws IOException {

        String source = readSource();

        assertThat(source)
                .contains(
                        "Feature.MULTI_USER"
                );

        assertThat(source)
                .contains(
                        "AdminRole.STORE_OWNER"
                );

        assertThat(source)
                .contains(
                        "AdminRole.STORE_ADMIN"
                );
    }

    private String readSource()
            throws IOException {

        return java.nio.file.Files.readString(
                java.nio.file.Path.of(
                        "src/main/java/"
                        + "com/webempresarial/store/"
                        + "feature/catalog/"
                        + "PlatformFeatures.java"
                ),
                StandardCharsets.UTF_8
        );
    }
}
