package com.webempresarial.store.event;

import com.webempresarial.store.service.AdminAccountInvitationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(
        AdminAccountInvitationAfterCommitTest.TestConfig.class
)
class AdminAccountInvitationAfterCommitTest {

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        AdminAccountInvitationService invitationService() {
            return mock(
                    AdminAccountInvitationService.class
            );
        }

        @Bean
        AdminAccountInvitationListener invitationListener(
                AdminAccountInvitationService invitationService
        ) {
            return new AdminAccountInvitationListener(
                    invitationService
            );
        }

        @Bean
        PlatformTransactionManager transactionManager() {

            return new AbstractPlatformTransactionManager() {

                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(
                        Object transaction,
                        TransactionDefinition definition
                ) {
                    // No resource required.
                }

                @Override
                protected void doCommit(
                        DefaultTransactionStatus status
                ) {
                    // No resource required.
                }

                @Override
                protected void doRollback(
                        DefaultTransactionStatus status
                ) {
                    // No resource required.
                }
            };
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AdminAccountInvitationService invitationService;

    @BeforeEach
    void resetMocks() {
        reset(invitationService);
    }

    @Test
    void shouldSendInvitationOnlyAfterTransactionCommits()
            throws Exception {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        101L,
                        "owner@acme.test",
                        "Alice Owner",
                        "ACME",
                        "acme.web-empresarial.com",
                        "activation-token-123"
                );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );

        transactionTemplate.executeWithoutResult(
                status -> {

                    eventPublisher.publishEvent(event);

                    /*
                     * AFTER_COMMIT todavía no debe
                     * ejecutarse dentro de la transacción.
                     */
                    try {
                        verify(
                                invitationService,
                                never()
                        ).sendActivationInvitation(
                                any()
                        );
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );

        /*
         * La transacción terminó con COMMIT.
         * Ahora el listener debe haberse ejecutado.
         */
        verify(invitationService)
                .sendActivationInvitation(event);
    }

    @Test
    void shouldNotSendInvitationWhenTransactionRollsBack()
            throws Exception {

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        102L,
                        "owner@rollback.test",
                        "Rollback Owner",
                        "Rollback Store",
                        "acme.web-empresarial.com",
                        "rollback-token"
                );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        transactionManager
                );

        transactionTemplate.executeWithoutResult(
                status -> {

                    eventPublisher.publishEvent(event);

                    status.setRollbackOnly();
                }
        );

        /*
         * No hubo COMMIT:
         * AFTER_COMMIT no debe ejecutarse.
         */
        verify(
                invitationService,
                never()
        ).sendActivationInvitation(
                any()
        );
    }
}
