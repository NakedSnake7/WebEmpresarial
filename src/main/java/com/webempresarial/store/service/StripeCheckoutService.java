package com.webempresarial.store.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.model.Order;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeCheckoutService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createSession(Order order) throws StripeException {

        long amountInCents = BigDecimal.valueOf(order.getTotal())
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String baseUrl = "https://" + order.getStore().getDominio();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)

                        .setSuccessUrl(
                                baseUrl + "/gracias?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId()
                        )
                        .setCancelUrl(
                                baseUrl + "/checkout-cancel?order_id=" + order.getId()
                        )

                        .setCustomerEmail(order.getCustomerEmail())

                        .putMetadata("order_id", order.getId().toString())
                        .putMetadata("store_id", order.getStore().getId().toString())
                        .putMetadata("payment_method", "STRIPE")
                        .putMetadata("store", order.getStore().getNombre())
                        .putMetadata("theme", order.getStore().getTheme())
                        .putMetadata("env", environment)

                        .setClientReferenceId("ORDER-" + order.getId())

                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("mxn")
                                                        .setUnitAmount(amountInCents)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Orden #" + order.getId() + " – " + order.getStore().getNombre())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(
                        "store_" + order.getStore().getId() + "_order_" + order.getId()
                )
                .build();

        return Session.create(params, options);
    }

    public String getSessionUrl(String sessionId) throws StripeException {

        Session session = Session.retrieve(sessionId);

        if (session == null || session.getUrl() == null) {
            throw new IllegalStateException("Sesión Stripe no válida o expirada");
        }

        return session.getUrl();
    }
    
    
    public boolean isSessionExpired(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return "expired".equals(session.getStatus());
        } catch (Exception e) {
            return true;
        }
    }


}
