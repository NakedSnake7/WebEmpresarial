package com.webempresarial.store.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${app.environment:prod}")
    private String environment;

    public StripeConnectService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public String createOnboardingLink(Store store) {
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
                            .setRefreshUrl(resolveBaseUrl() + "/admin/stripe/connect/refresh")
                            .setReturnUrl(resolveBaseUrl() + "/admin/stripe/connect/return")
                            .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                            .build();

            AccountLink accountLink = AccountLink.create(linkParams);

            return accountLink.getUrl();

        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("No se pudo iniciar Stripe Connect onboarding: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void markConnected(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        store.setStripeConnected(true);
        store.setStripeConnectedAt(LocalDateTime.now());

        storeRepository.save(store);
    }

    private String resolveBaseUrl() {
        if ("dev".equalsIgnoreCase(environment) || "local".equalsIgnoreCase(environment)) {
            return "http://localhost:8080";
        }

        return "https://web-empresarial.com";
    }
}