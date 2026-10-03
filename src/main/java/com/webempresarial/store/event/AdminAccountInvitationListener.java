package com.webempresarial.store.event;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.webempresarial.store.service.AdminAccountInvitationService;

@Component
public class AdminAccountInvitationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AdminAccountInvitationListener.class
            );

    private final AdminAccountInvitationService
            invitationService;

    public AdminAccountInvitationListener(
            AdminAccountInvitationService invitationService
    ) {
        this.invitationService =
                invitationService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onAdminAccountCreated(
            AdminAccountCreatedEvent event
    ) {

        try {

            invitationService
                    .sendActivationInvitation(event);

        } catch (IOException ex) {

            /*
             * El tenant ya fue provisionado.
             *
             * Un fallo de MailerSend NO debe provocar
             * rollback ni reprovisionamiento.
             */
            log.error(
                    "No fue posible enviar la invitación "
                    + "de activación al administrador {}",
                    event.adminUserId(),
                    ex
            );
        }
    }
}