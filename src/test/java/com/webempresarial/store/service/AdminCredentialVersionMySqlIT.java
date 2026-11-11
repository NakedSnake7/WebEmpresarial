package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        initializers = AdminCredentialVersionMySqlIT.TestDatabaseGuard.class
)
@Import({
        AdminPasswordChangeService.class,
        AdminCredentialVersionMySqlIT.PasswordConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminCredentialVersionMySqlIT {

    private static final String DATABASE =
            "webempresarial_flyway_test";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/" + DATABASE;

    private static final String CURRENT =
            "ClaveActualSegura#2026";

    private static final String NEXT =
            "ClaveNuevaSegura#2026";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AdminUserRepository repository;

    @Autowired
    private AdminPasswordChangeService passwordService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String email;
    private Long userId;

    /*
     * Se ejecuta antes de inicializar Flyway y JPA.
     */
    public static class TestDatabaseGuard
            implements ApplicationContextInitializer<
                    ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context
        ) {
            var environment = context.getEnvironment();

            String datasourceUrl =
                    environment.getProperty(
                            "spring.datasource.url"
                    );

            String flywayUrl =
                    environment.getProperty(
                            "spring.flyway.url"
                    );

            if (!URL.equals(datasourceUrl)) {
                throw new IllegalStateException(
                        "Base de datos no autorizada para IT: "
                        + datasourceUrl
                );
            }

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {

                throw new IllegalStateException(
                        "Flyway apunta a una base no autorizada"
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
    void createIsolatedAdmin() throws Exception {

        verifyDatabase();

        email = "credential-it-"
                + UUID.randomUUID()
                + "@example.invalid";

        AdminUser admin = new AdminUser();
        admin.setFullName("Credential Integration Test");
        admin.setEmail(email);
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword(encoder.encode(CURRENT));

        userId = repository.saveAndFlush(admin).getId();

        assertNotNull(userId);
    }

    @AfterEach
    void deleteIsolatedAdmin() throws Exception {

        if (userId != null) {
            verifyDatabase();
            repository.deleteById(userId);
        }
    }

    private void verifyDatabase() throws Exception {

        try (Connection connection =
                     dataSource.getConnection()) {

            assertEquals(
                    DATABASE,
                    connection.getCatalog(),
                    "Está prohibido utilizar otra base de datos"
            );
        }
    }

    @Test
    void flywayV37IsApplied() throws Exception {

        try (Connection connection =
                     dataSource.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             "SELECT COUNT(*) " +
                             "FROM flyway_schema_history " +
                             "WHERE version = '37' AND success = 1"
                     );

             ResultSet result = statement.executeQuery()) {

            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
        }

        AdminUser admin = repository
                .findByEmail(email)
                .orElseThrow();

        assertEquals(
                0L,
                admin.getCredentialVersion()
        );
    }

    @Test
    void passwordChangePersistsCredentialRotation() {

        passwordService.changeOwnPassword(
                email,
                CURRENT,
                NEXT
        );

        AdminUser updated = repository
                .findByEmail(email)
                .orElseThrow();

        assertEquals(
                1L,
                updated.getCredentialVersion()
        );

        assertTrue(
                encoder.matches(
                        NEXT,
                        updated.getPassword()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.changeOwnPassword(
                        email,
                        CURRENT,
                        "OtraClaveSegura#2026"
                )
        );

        AdminUser afterRejectedChange = repository
                .findByEmail(email)
                .orElseThrow();

        assertEquals(
                1L,
                afterRejectedChange.getCredentialVersion()
        );
    }

    @Test
    void concurrentTransactionsDoNotLoseCredentialRotations()
            throws Exception {

        TransactionTemplate transactions =
                new TransactionTemplate(transactionManager);

        CountDownLatch firstHasLock =
                new CountDownLatch(1);

        CountDownLatch releaseFirst =
                new CountDownLatch(1);

        CountDownLatch secondStarting =
                new CountDownLatch(1);

        CountDownLatch secondHasLock =
                new CountDownLatch(1);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {

            Future<?> first = executor.submit(() ->
                    transactions.executeWithoutResult(status -> {

                        AdminUser admin = repository
                                .findByEmailForUpdate(email)
                                .orElseThrow();

                        firstHasLock.countDown();

                        awaitOrThrow(releaseFirst);

                        admin.rotateCredentialVersion();

                        repository.saveAndFlush(admin);
                    })
            );

            assertTrue(
                    firstHasLock.await(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "La primera transacción no obtuvo el lock"
            );

            Future<?> second = executor.submit(() ->
                    transactions.executeWithoutResult(status -> {

                        secondStarting.countDown();

                        AdminUser admin = repository
                                .findByEmailForUpdate(email)
                                .orElseThrow();

                        secondHasLock.countDown();

                        assertEquals(
                                1L,
                                admin.getCredentialVersion(),
                                "La segunda transacción debe leer " +
                                "la versión confirmada por la primera"
                        );

                        admin.rotateCredentialVersion();

                        repository.saveAndFlush(admin);
                    })
            );

            try {

                assertTrue(
                        secondStarting.await(
                                10,
                                TimeUnit.SECONDS
                        )
                );

                assertFalse(
                        secondHasLock.await(
                                300,
                                TimeUnit.MILLISECONDS
                        ),
                        "La segunda transacción obtuvo " +
                        "el lock antes de liberar la primera"
                );

            } finally {
                releaseFirst.countDown();
            }

            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);

            AdminUser updated = repository
                    .findByEmail(email)
                    .orElseThrow();

            assertEquals(
                    2L,
                    updated.getCredentialVersion(),
                    "Se perdió un incremento concurrente"
            );

        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    private static void awaitOrThrow(
            CountDownLatch latch
    ) {
        try {

            if (!latch.await(
                    20,
                    TimeUnit.SECONDS
            )) {
                throw new IllegalStateException(
                        "Tiempo excedido esperando liberación"
                );
            }

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Prueba interrumpida",
                    exception
            );
        }
    }
}
