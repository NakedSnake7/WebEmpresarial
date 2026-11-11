package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.AdminPasswordResetToken;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.AdminPasswordResetTokenRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

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
        AdminPasswordResetConcurrencyMySqlIT.DatabaseGuard.class
)
@Import({
    AdminPasswordResetService.class,
    AdminPasswordResetTokenCodec.class,
    AdminPasswordResetConcurrencyMySqlIT.PasswordConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminPasswordResetConcurrencyMySqlIT {

    private static final String DATABASE =
            "webempresarial_flyway_test";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/" + DATABASE;

    private static final String ORIGINAL =
            "ClaveOriginalSegura#2026";

    private static final String FIRST =
            "PrimeraClaveSegura#2026";

    private static final String SECOND =
            "SegundaClaveSegura#2026";

    @Autowired
    private DataSource datasource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AdminUserRepository admins;

    @Autowired
    private AdminPasswordResetTokenRepository tokens;

    @Autowired
    private AdminPasswordResetTokenCodec codec;

    @Autowired
    private AdminPasswordResetService reset;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private PlatformTransactionManager txManager;

    private Long adminId;
    private String email;

    public static class DatabaseGuard
            implements ApplicationContextInitializer<
                ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context
        ) {

            var env = context.getEnvironment();

            String datasourceUrl = env.getProperty(
                    "spring.datasource.url"
            );

            String flywayUrl = env.getProperty(
                    "spring.flyway.url"
            );

            if (!URL.equals(datasourceUrl)) {
                throw new IllegalStateException(
                    "Base no autorizada: " + datasourceUrl
                );
            }

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {

                throw new IllegalStateException(
                    "Flyway apunta a otra base"
                );
            }
        }
    }

    @TestConfiguration
    static class PasswordConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @BeforeEach
    void prepare() throws Exception {

        verifyDatabase();

        email = "reset-concurrency-"
                + UUID.randomUUID()
                + "@example.invalid";

        AdminUser admin = new AdminUser();
        admin.setFullName("Reset Concurrency IT");
        admin.setEmail(email);
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword(encoder.encode(ORIGINAL));

        adminId = admins.saveAndFlush(admin).getId();

        assertNotNull(adminId);
    }

    @AfterEach
    void cleanup() throws Exception {

        if (adminId != null) {

            verifyDatabase();

            jdbc.update(
                "DELETE FROM admin_password_reset_tokens " +
                "WHERE admin_user_id = ?",
                adminId
            );

            admins.deleteById(adminId);
        }
    }

    private void verifyDatabase() throws Exception {

        try (Connection c = datasource.getConnection()) {
            assertEquals(DATABASE, c.getCatalog());
        }
    }

    private String createToken() {

        AdminUser admin = admins.findById(adminId)
                .orElseThrow();

        String raw = codec.generateRawToken();

        LocalDateTime now = LocalDateTime.ofInstant(
                Instant.now(),
                ZoneOffset.UTC
        );

        tokens.saveAndFlush(
            AdminPasswordResetToken.issue(
                admin,
                codec.hashToken(raw),
                now.minusSeconds(5),
                now.plusMinutes(20)
            )
        );

        return raw;
    }

    @Test
    void issuingTwiceDuringCooldownLeavesOneToken() {

        reset.requestRecovery(email);

        assertEquals(
            1,
            tokens.findByAdminUserIdAndConsumedAtIsNull(
                adminId
            ).size()
        );

        reset.requestRecovery(email);

        assertEquals(
            1,
            tokens.findByAdminUserIdAndConsumedAtIsNull(
                adminId
            ).size()
        );
    }

    @Test
    void rejectedPasswordLeavesTokenUnconsumed() {

        String raw = createToken();

        assertThrows(
            IllegalArgumentException.class,
            () -> reset.completeRecovery(raw, "corta")
        );

        AdminUser admin = admins.findById(adminId)
                .orElseThrow();

        assertEquals(0L, admin.getCredentialVersion());

        assertTrue(
            encoder.matches(
                ORIGINAL,
                admin.getPassword()
            )
        );

        assertNull(
            tokens.findByTokenHash(codec.hashToken(raw))
                .orElseThrow()
                .getConsumedAt()
        );
    }

    @Test
    void concurrentRedemptionAllowsExactlyOneSuccess()
            throws Exception {

        String raw = createToken();

        TransactionTemplate tx =
                new TransactionTemplate(txManager);

        CountDownLatch firstChanged =
                new CountDownLatch(1);

        CountDownLatch releaseFirst =
                new CountDownLatch(1);

        CountDownLatch secondStarted =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {

            Future<?> first = executor.submit(() ->
                tx.executeWithoutResult(status -> {

                    reset.completeRecovery(raw, FIRST);

                    firstChanged.countDown();

                    try {
                        if (!releaseFirst.await(
                                20,
                                TimeUnit.SECONDS
                        )) {
                            throw new IllegalStateException(
                                "Timeout esperando liberación"
                            );
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                })
            );

            assertTrue(
                firstChanged.await(15, TimeUnit.SECONDS),
                "La primera recuperación no llegó al cambio"
            );

            Future<Throwable> second =
                executor.submit(() -> {

                    secondStarted.countDown();

                    try {
                        reset.completeRecovery(raw, SECOND);
                        return null;
                    } catch (Throwable ex) {
                        return ex;
                    }
                });

            try {

                assertTrue(
                    secondStarted.await(
                        10,
                        TimeUnit.SECONDS
                    )
                );

                assertFalse(
                    second.isDone(),
                    "El segundo canje terminó prematuramente"
                );

            } finally {
                releaseFirst.countDown();
            }

            first.get(20, TimeUnit.SECONDS);

            Throwable secondResult =
                    second.get(20, TimeUnit.SECONDS);

            assertInstanceOf(
                IllegalArgumentException.class,
                secondResult,
                "El segundo canje debe ser rechazado"
            );

            AdminUser updated = admins.findById(adminId)
                    .orElseThrow();

            assertEquals(
                1L,
                updated.getCredentialVersion()
            );

            assertTrue(
                encoder.matches(
                    FIRST,
                    updated.getPassword()
                )
            );

            assertFalse(
                encoder.matches(
                    SECOND,
                    updated.getPassword()
                )
            );

            assertNotNull(
                tokens.findByTokenHash(
                    codec.hashToken(raw)
                )
                .orElseThrow()
                .getConsumedAt()
            );

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void passwordRotationMakesPreviousLinkUnusable() {

        String raw = createToken();

        AdminUser admin =
                admins.findById(adminId).orElseThrow();

        admin.setPassword(encoder.encode(SECOND));
        admin.rotateCredentialVersion();

        admins.saveAndFlush(admin);

        assertThrows(
                IllegalArgumentException.class,
                () -> reset.completeRecovery(raw, FIRST)
        );

        AdminUser updated =
                admins.findById(adminId).orElseThrow();

        assertEquals(
                1L,
                updated.getCredentialVersion()
        );

        assertTrue(
                encoder.matches(
                        SECOND,
                        updated.getPassword()
                )
        );

        assertFalse(
                encoder.matches(
                        FIRST,
                        updated.getPassword()
                )
        );

        assertNull(
                tokens.findByTokenHash(
                        codec.hashToken(raw)
                )
                .orElseThrow()
                .getConsumedAt()
        );
    }

}
