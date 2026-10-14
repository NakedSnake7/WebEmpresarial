package com.webempresarial.store.service;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class StoreOwnerMigrationContractTest {

    @Test
    void migrationShouldPromoteOnlyMatchingStoreAdminEmail()
            throws IOException {

        String sql =
                new ClassPathResource(
                        "db/migration/"
                        + "V26__promote_existing_store_owners.sql"
                )
                .getContentAsString(
                        StandardCharsets.UTF_8
                );

        assertThat(sql)
                .contains(
                        "SET au.role = 'STORE_OWNER'"
                );

        assertThat(sql)
                .contains(
                        "au.role = 'STORE_ADMIN'"
                );

        assertThat(sql)
                .contains(
                        "s.company_email IS NOT NULL"
                );

        assertThat(sql)
                .contains(
                        "LOWER(TRIM(au.email))"
                );

        assertThat(sql)
                .contains(
                        "LOWER(TRIM(s.company_email))"
                );
    }

    @Test
    void migrationMustNotPromoteEveryStoreAdminBlindly()
            throws IOException {

        String sql =
                new ClassPathResource(
                        "db/migration/"
                        + "V26__promote_existing_store_owners.sql"
                )
                .getContentAsString(
                        StandardCharsets.UTF_8
                );

        assertThat(sql)
                .doesNotContain(
                        """
                        UPDATE admin_users
                        SET role = 'STORE_OWNER'
                        WHERE role = 'STORE_ADMIN';
                        """
                );
    }
}
