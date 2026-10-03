package com.webempresarial.store.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class StripeConnectService {

    private final StoreRepository storeRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;


    public StripeConnectService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private static final Logger log =
            LoggerFactory.getLogger(
                    StripeConnectService.class
            );

    @Transactional
    public String createOnboardingLink(
            Store store,
            String baseUrl
    )    {
        try {
            String accountId = store.getStripeConnectedAccountId();

            if (accountId == null || accountId.isBlank()) {
                AccountCreateParams accountParams =
                        AccountCreateParams.builder()
                                .setType(AccountCreateParams.Type.STANDARD)
                                .setEmail(store.getCompanyEmail())
                                .build();

                Account account = Account.create(accountParams);

                accountId = account.getId();

                store.setStripeConnectedAccountId(accountId);
                store.setStripeConnected(false);
                storeRepository.save(store);
            }

            AccountLinkCreateParams linkParams =
                    AccountLinkCreateParams.builder()
                            .setAccount(accountId)
                            .setRefreshUrl(
                                    baseUrl
                                            + "/admin/stripe/connect/refresh"
                            )
                            .setReturnUrl(
                                    baseUrl
                                            + "/admin/stripe/connect/return"
                            )
                            .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                            .build();

            AccountLink accountLink = AccountLink.create(linkParams);

            return accountLink.getUrl();

        } catch (Exception e) {

            log.error(
                    "No se pudo iniciar Stripe Connect onboarding "
                            + "para storeId={}",
                    store != null ? store.getId() : null,
                    e
            );

            throw new RuntimeException(
                    "No se pudo iniciar Stripe Connect onboarding",
                    e
            );
        }
    }

    @Transactional
    public boolean syncConnectionStatus(
            Store store
    ) {

        String accountId =
                store.getStripeConnectedAccountId();

        if (accountId == null
                || accountId.isBlank()) {

            store.setStripeConnected(false);
            store.setStripeConnectedAt(null);

            storeRepository.save(store);

            return false;
        }

        try {

            Account account =
                    Account.retrieve(accountId);

            boolean connected =
                    Boolean.TRUE.equals(
                            account.getChargesEnabled()
                    );

            store.setStripeConnected(connected);

            if (connected) {

                if (store.getStripeConnectedAt() == null) {
                    store.setStripeConnectedAt(
                            LocalDateTime.now()
                    );
                }

            } else {

                store.setStripeConnectedAt(null);
            }

            storeRepository.save(store);

            return connected;

        } catch (Exception e) {

            store.setStripeConnected(false);
            store.setStripeConnectedAt(null);

            storeRepository.save(store);

            throw new RuntimeException(
                    "No se pudo verificar el estado de Stripe Connect",
                    e
            );
        }
    }


}