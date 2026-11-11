package com.webempresarial.store.event;

import com.webempresarial.store.service.AdminPasswordResetMailService;
import com.webempresarial.store.service.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webempresarial_flyway_test",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.hibernate.ddl-auto=validate",
    "app.base-url=https://web-empresarial.com"
})
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("dev")
@ContextConfiguration(
    initializers =
        AdminPasswordResetMailAfterCommitIT.DatabaseGuard.class
)
@Import({
    AdminPasswordResetMailService.class,
    AdminPasswordResetMailListener.class,
    AdminPasswordResetMailAfterCommitIT.ExecutorTestConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminPasswordResetMailAfterCommitIT {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/"
            + "webempresarial_flyway_test";

    private static final String RAW =
            "A".repeat(43);

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private DeferredExecutor executor;

    @MockitoBean
    private EmailService emailService;

    public static class DatabaseGuard
            implements ApplicationContextInitializer<
                    ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context
        ) {

            var env = context.getEnvironment();

            if (!URL.equals(
                    env.getProperty("spring.datasource.url")
            )) {
                throw new IllegalStateException(
                        "Base de datos no autorizada"
                );
            }

            String flywayUrl =
                    env.getProperty("spring.flyway.url");

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {

                throw new IllegalStateException(
                        "Flyway apunta a otra base"
                );
            }
        }
    }

    static final class DeferredExecutor implements Executor {

        private final Deque<Runnable> pending =
                new ArrayDeque<>();

        @Override
        public synchronized void execute(Runnable task) {
            pending.addLast(task);
        }

        synchronized int pendingCount() {
            return pending.size();
        }

        synchronized void clear() {
            pending.clear();
        }

        void runAll() {

            while (true) {

                Runnable task;

                synchronized (this) {
                    task = pending.pollFirst();
                }

                if (task == null) {
                    return;
                }

                task.run();
            }
        }
    }

    @TestConfiguration
    static class ExecutorTestConfig {

        @Bean(name = "adminPasswordResetMailExecutor")
        DeferredExecutor deferredExecutor() {
            return new DeferredExecutor();
        }
    }

    @BeforeEach
    void resetQueue() {
        executor.clear();
    }

    private AdminPasswordResetIssuedEvent event() {

        return new AdminPasswordResetIssuedEvent(
                "admin@example.invalid",
                "Admin Test",
                RAW
        );
    }

    @Test
    void queuesOnlyAfterSuccessfulCommit() throws Exception {

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status -> {

            publisher.publishEvent(event());

            assertEquals(0, executor.pendingCount());
            verifyNoInteractions(emailService);
        });

        assertEquals(1, executor.pendingCount());

        /*
         * La transacción terminó, pero la entrega
         * todavía no se ha ejecutado.
         */
        verifyNoInteractions(emailService);

        executor.runAll();

        verify(emailService, times(1))
                .enviarCorreoHTML(
                        eq("admin@example.invalid"),
                        eq(
                            "Recupera tu contraseña de WebEmpresarial"
                        ),
                        contains(RAW)
                );
    }

    @Test
    void rollbackDoesNotQueueOrSend() {

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status -> {

            publisher.publishEvent(event());

            status.setRollbackOnly();
        });

        assertEquals(0, executor.pendingCount());

        executor.runAll();

        verifyNoInteractions(emailService);
    }

    @Test
    void mailDeliveryFailureDoesNotEscapeWorker()
            throws Exception {

        doThrow(new IOException("Simulated provider failure"))
                .when(emailService)
                .enviarCorreoHTML(
                        anyString(),
                        anyString(),
                        anyString()
                );

        TransactionTemplate tx =
                new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(status ->
                publisher.publishEvent(event())
        );

        assertEquals(1, executor.pendingCount());

        assertDoesNotThrow(executor::runAll);

        assertEquals(0, executor.pendingCount());
    }
}
