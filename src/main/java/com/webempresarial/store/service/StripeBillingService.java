package com.webempresarial.store.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingService {

    private final StripePlanMapper stripePlanMapper;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    public StripeBillingService(StripePlanMapper stripePlanMapper) {
        this.stripePlanMapper = stripePlanMapper;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createCheckoutSession(
            Store store,
            StorePlan plan
    ) throws Exception {

        String priceId = stripePlanMapper.getPriceId(plan);
        String storeBaseUrl = resolveStoreBaseUrl(store);

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)

                        .setSuccessUrl(
                                storeBaseUrl
                                        + "/admin/billing/success"
                                        + "?session_id={CHECKOUT_SESSION_ID}"
                        )

                        .setCancelUrl(
                                storeBaseUrl
                                        + "/admin/billing?cancelled=true"
                        )

                        .putMetadata(
                                "checkout_type",
                                "SAAS_SUBSCRIPTION_EXISTING_STORE"
                        )
                        .putMetadata(
                                "store_id",
                                store.getId().toString()
                        )
                        .putMetadata(
                                "plan",
                                plan.name()
                        )
                        .putMetadata(
                                "stripe_price_id",
                                priceId
                        )
                        .putMetadata(
                                "env",
                                environment
                        )

                        .setClientReferenceId(
                                "STORE-"
                                        + store.getId()
                                        + "-"
                                        + plan.name()
                        )

                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(
                        "store_"
                                + store.getId()
                                + "_subscription_"
                                + plan.name()
                                + "_"
                                + java.util.UUID.randomUUID()
                )
                .build();

        Session session = Session.create(params, options);

        return session.getUrl();
    }

    private String resolveStoreBaseUrl(Store store) {

        if (store == null
                || store.getDominio() == null
                || store.getDominio().isBlank()) {
            throw new IllegalStateException(
                    "La tienda no tiene dominio configurado"
            );
        }

        String domain = store.getDominio()
                .trim()
                .toLowerCase();

        if (domain.endsWith(".local")) {
            return "http://" + domain + ":8080";
        }

        if (domain.contains("localhost")) {
            return domain.startsWith("http")
                    ? domain
                    : "http://" + domain;
        }

        return domain.startsWith("http")
                ? domain
                : "https://" + domain;
    }
}