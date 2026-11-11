package com.webempresarial.store.repository;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.AdminPasswordResetToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webempresarial_flyway_test",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("dev")
@ContextConfiguration(
    initializers =
        AdminPasswordResetTokenMySqlIT.DatabaseGuard.class
)
class AdminPasswordResetTokenMySqlIT {

    private static final String DATABASE =
            "webempresarial_flyway_test";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/" + DATABASE;

    private static final String HASH =
            "ab".repeat(32);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AdminUserRepository admins;

    @Autowired
    private AdminPasswordResetTokenRepository tokens;

    public static class DatabaseGuard
            implements ApplicationContextInitializer<
                    ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context
        ) {

            var environment = context.getEnvironment();

            String url = environment.getProperty(
                    "spring.datasource.url"
            );

            String flywayUrl = environment.getProperty(
                    "spring.flyway.url"
            );

            if (!URL.equals(url)) {
                throw new IllegalStateException(
                        "Base MySQL no autorizada: " + url
                );
            }

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {

                throw new IllegalStateException(
                        "Flyway utiliza otra base de datos"
                );
            }
        }
    }

    @BeforeEach
    void verifyDatabaseIsolation() throws Exception {

        try (Connection connection =
                dataSource.getConnection()) {

            assertEquals(
                    DATABASE,
                    connection.getCatalog(),
                    "No se permite utilizar webempresarial_dev"
            );
        }
    }

    @Test
    void flywayV38AndUniqueIndexExist() {

        Integer migrationCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '38'
                  AND success = 1
                """,
                Integer.class
        );

        assertEquals(1, migrationCount);

        Integer indexCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name =
                      'admin_password_reset_tokens'
                  AND index_name =
                      'uk_admin_password_reset_token_hash'
                  AND non_unique = 0
                """,
                Integer.class
        );

        assertEquals(1, indexCount);
    }

    @Test
    void persistsLocksAndConsumesToken() {

        AdminUser admin = new AdminUser();

        admin.setFullName("Password Reset Test");

        admin.setEmail(
                "reset-it-"
                + UUID.randomUUID()
                + "@example.invalid"
        );

        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword("test-hash-not-for-login");

        admins.saveAndFlush(admin);

        LocalDateTime created =
                LocalDateTime.of(
                        2026, 11, 10, 12, 0
                );

        AdminPasswordResetToken token =
                AdminPasswordResetToken.issue(
                        admin,
                        HASH,
                        created,
                        created.plusMinutes(20)
                );

        tokens.saveAndFlush(token);

        AdminPasswordResetToken locked =
                tokens.findByTokenHashForUpdate(HASH)
                        .orElseThrow();

        assertEquals(
                admin.getId(),
                locked.getAdminUser().getId()
        );

        assertTrue(
                locked.isUsableAt(
                        created.plusMinutes(1)
                )
        );

        locked.consume(
                created.plusMinutes(1)
        );

        tokens.saveAndFlush(locked);

        entityManager.clear();

        AdminPasswordResetToken reloaded =
                tokens.findByTokenHash(HASH)
                        .orElseThrow();

        assertNotNull(
                reloaded.getConsumedAt()
        );

        assertFalse(
                reloaded.isUsableAt(
                        created.plusMinutes(2)
                )
        );
    }

    @Test
    void flywayV39PersistsCredentialSnapshot() {

        Integer applied = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '39'
                  AND success = 1
                """,
                Integer.class
        );

        assertEquals(1, applied);

        AdminUser admin = new AdminUser();
        admin.setFullName("Reset Version IT");
        admin.setEmail(
                "reset-version-"
                + UUID.randomUUID()
                + "@example.invalid"
        );
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword("test-hash-not-for-login");
        admin.rotateCredentialVersion();

        admins.saveAndFlush(admin);

        LocalDateTime created =
                LocalDateTime.of(
                        2026, 11, 10, 12, 0
                );

        String hash = "ef".repeat(32);

        tokens.saveAndFlush(
                AdminPasswordResetToken.issue(
                        admin,
                        hash,
                        created,
                        created.plusMinutes(20)
                )
        );

        entityManager.clear();

        AdminPasswordResetToken stored =
                tokens.findByTokenHash(hash)
                        .orElseThrow();

        assertEquals(
                1L,
                stored.getCredentialVersionAtIssue()
        );
    }

}
