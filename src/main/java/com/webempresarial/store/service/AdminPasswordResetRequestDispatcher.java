package com.webempresarial.store.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Service
public class AdminPasswordResetRequestDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AdminPasswordResetRequestDispatcher.class
            );

    private final AdminPasswordResetService recoveryService;
    private final Executor executor;

    public AdminPasswordResetRequestDispatcher(
            AdminPasswordResetService recoveryService,
            @Qualifier("adminPasswordResetRequestExecutor")
            Executor executor
    ) {
        this.recoveryService = recoveryService;
        this.executor = executor;
    }

    public void dispatch(String submittedEmail) {

        if (submittedEmail == null
                || submittedEmail.isBlank()
                || submittedEmail.length() > 254) {
            return;
        }

        String email = submittedEmail.trim();

        try {

            /*
             * La solicitud HTTP termina sin esperar
             * la consulta de AdminUser ni la creación
             * del token.
             *
             * requestRecovery() conserva su propia
             * transacción de Spring.
             */
            executor.execute(() -> {

                try {

                    recoveryService.requestRecovery(email);

                } catch (RuntimeException ex) {

                    /*
                     * No registrar correos, tokens
                     * ni argumentos de la excepción.
                     */
                    log.warn(
                            "Error procesando recuperación "
                            + "administrativa ({})",
                            ex.getClass().getSimpleName()
                    );
                }
            });

        } catch (RejectedExecutionException ex) {

            /*
             * Una cola saturada no revela al cliente
             * si el correo existe.
             */
            log.warn(
                    "Cola de solicitudes de recuperación "
                    + "administrativa saturada"
            );
        }
    }
}
