package com.webempresarial.store.service;

import com.webempresarial.store.entity.AdminAccountActivationToken;
import com.webempresarial.store.event.AdminAccountCreatedEvent;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminAccountActivationTokenRepository;
import com.webempresarial.store.repository.AdminUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class AdminAccountInvitationResendService {

    private final AdminUserRepository
            adminUserRepository;

    private final AdminAccountActivationTokenRepository
            tokenRepository;

    private final AdminAccountInvitationService
            invitationService;

    private final AdminAccountActivationService
            activationService;

    public AdminAccountInvitationResendService(
            AdminUserRepository adminUserRepository,
            AdminAccountActivationTokenRepository tokenRepository,
            AdminAccountInvitationService invitationService,
            AdminAccountActivationService activationService
    ) {
        this.adminUserRepository =
                adminUserRepository;

        this.tokenRepository =
                tokenRepository;

        this.invitationService =
                invitationService;

        this.activationService =
                activationService;
    }

    @Transactional
    public void resend(
            Long adminUserId
    ) throws IOException {

        AdminUser admin =
                adminUserRepository
                        .findById(adminUserId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Administrador no encontrado"
                                        )
                        );

        if (admin.isEnabled()) {
            throw new IllegalStateException(
                    "La cuenta ya está activada"
            );
        }

        Store store =
                admin.getStore();

        if (store == null) {
            throw new IllegalStateException(
                    "El administrador no pertenece "
                    + "a una tienda"
            );
        }

        Optional<AdminAccountActivationToken>
                pendingToken =
                tokenRepository
                        .findFirstByAdminUserIdAndUsedFalseOrderByCreatedAtDesc(
                                adminUserId
                        );

        AdminAccountActivationToken token;

        if (pendingToken.isPresent()
                && pendingToken.get().isUsable()) {

            token = pendingToken.get();

        } else {

            token =
                    activationService
                            .createToken(admin);
        }

        AdminAccountCreatedEvent event =
                new AdminAccountCreatedEvent(
                        admin.getId(),
                        admin.getEmail(),
                        admin.getFullName(),
                        store.getNombre(),
                        store.getDominio(),
                        token.getToken()
                );

        invitationService
                .sendActivationInvitation(event);
    }
}