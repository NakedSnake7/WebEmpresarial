package com.webempresarial.store.event;

import com.webempresarial.store.service.AdminPasswordResetMailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Component
public class AdminPasswordResetMailListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AdminPasswordResetMailListener.class
            );

    private final AdminPasswordResetMailService mailService;
    private final Executor executor;

    public AdminPasswordResetMailListener(
            AdminPasswordResetMailService mailService,
            @Qualifier("adminPasswordResetMailExecutor")
            Executor executor
    ) {
        this.mailService = mailService;
        this.executor = executor;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onRecoveryIssued(
            AdminPasswordResetIssuedEvent event
    ) {

        /*
         * Se programa después del commit.
         * La petición HTTP no espera a MailerSend.
         *
         * Los tokens no se escriben en logs.
         */
        try {

            executor.execute(() -> {

                try {

                    mailService.sendRecoveryEmail(event);

                } catch (IOException | RuntimeException ex) {

                    log.warn(
                            "Fallo de entrega de recuperación "
                            + "administrativa ({})",
                            ex.getClass().getSimpleName()
                    );
                }
            });

        } catch (RejectedExecutionException ex) {

            /*
             * Cola saturada o ejecutor detenido.
             * No imprimir el evento ni el token.
             */
            log.warn(
                    "No se pudo programar la entrega "
                    + "del correo de recuperación"
            );
        }
    }
}
