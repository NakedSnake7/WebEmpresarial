package com.webempresarial.store.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.billing.price.basic}")
    private String basicPriceId;

    @Value("${stripe.billing.price.pro}")
    private String proPriceId;

    @Value("${stripe.billing.price.premium}")
    private String premiumPriceId;

    @Value("${app.base-url}")
    private String baseUrl;

    public String createCheckoutSession(
            Store store,
            StorePlan plan
    ) throws Exception {

        Stripe.apiKey = stripeSecretKey;

        String priceId = resolvePriceId(plan);

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(baseUrl + "/admin/billing/success?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(baseUrl + "/admin/billing?cancelled=true")
                        .putMetadata("storeId", store.getId().toString())
                        .putMetadata("plan", plan.name())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        Session session = Session.create(params);

        return session.getUrl();
    }

    private String resolvePriceId(StorePlan plan) {

        return switch (plan) {
            case BASIC -> basicPriceId;
            case PRO -> proPriceId;
            case PREMIUM -> premiumPriceId;
        };
    }
}