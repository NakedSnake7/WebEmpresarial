package com.webempresarial.store.service;

import com.stripe.Stripe;
import com.stripe.model.billingportal.Session;
import com.stripe.param.billingportal.SessionCreateParams;
import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.SubscriptionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingPortalService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private final SubscriptionRepository subscriptionRepository;

    public StripeBillingPortalService(
            SubscriptionRepository subscriptionRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public String createPortalSession(Store store) throws Exception {

        Stripe.apiKey = stripeSecretKey;

        Subscription subscription =
                subscriptionRepository.findByStoreId(store.getId())
                        .orElseThrow(() ->
                                new RuntimeException("Suscripción no encontrada")
                        );

        if (subscription.getStripeCustomerId() == null ||
                subscription.getStripeCustomerId().isBlank()) {
            throw new RuntimeException("Esta suscripción no tiene customer de Stripe");
        }

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setCustomer(subscription.getStripeCustomerId())
                        .setReturnUrl(baseUrl + "/admin/billing")
                        .build();

        Session session = Session.create(params);

        return session.getUrl();
    }
}