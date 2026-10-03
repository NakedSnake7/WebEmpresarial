package com.webempresarial.store.commerce.infrastructure.checkout.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.model.Store;

import jakarta.annotation.PostConstruct;

@Service
public class StripeCommerceCheckoutService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createSession(
            Order order
    ) throws StripeException {

        if (order == null || order.getTotal() == null) {
            throw new IllegalArgumentException(
                    "La orden y su total son obligatorios"
            );
        }

        if (order.getTotal().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El total de la orden debe ser mayor a cero"
            );
        }

        Store store = requireStripeConnectedStore(
                order.getStore()
        );

        if (order.getId() == null) {
            throw new IllegalArgumentException(
                    "La orden debe estar persistida"
            );
        }

        long amountInCents =
                order.getTotal()
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .setScale(
                                0,
                                RoundingMode.HALF_UP
                        )
                        .longValueExact();

        String baseUrl =
                "https://" + store.getDominio();

        SessionCreateParams params =
                SessionCreateParams.builder()

                        .setMode(
                                SessionCreateParams.Mode.PAYMENT
                        )

                        .setSuccessUrl(
                                baseUrl
                                        + "/gracias"
                                        + "?session_id={CHECKOUT_SESSION_ID}"
                                        + "&order_id="
                                        + order.getId()
                        )

                        .setCancelUrl(
                                baseUrl
                                        + "/checkout-cancel"
                                        + "?order_id="
                                        + order.getId()
                        )

                        .setCustomerEmail(
                                order.getCustomerEmail()
                        )

                        .putMetadata(
                                "checkout_type",
                                "ECOMMERCE_ORDER"
                        )

                        .putMetadata(
                                "order_id",
                                order.getId().toString()
                        )

                        .putMetadata(
                                "store_id",
                                store.getId().toString()
                        )

                        .putMetadata(
                                "payment_method",
                                "STRIPE"
                        )

                        .putMetadata(
                                "store",
                                store.getNombre()
                        )

                        .putMetadata(
                                "theme",
                                store.getTheme() != null
                                        ? store.getTheme()
                                        : ""
                        )

                        .putMetadata(
                                "env",
                                environment
                        )

                        .setClientReferenceId(
                                "ORDER-" + order.getId()
                        )

                        .addLineItem(
                                SessionCreateParams.LineItem
                                        .builder()

                                        .setQuantity(1L)

                                        .setPriceData(
                                                SessionCreateParams
                                                        .LineItem
                                                        .PriceData
                                                        .builder()

                                                        .setCurrency(
                                                                resolveCurrency(
                                                                        store.getCurrency()
                                                                )
                                                        )

                                                        .setUnitAmount(
                                                                amountInCents
                                                        )

                                                        .setProductData(
                                                                SessionCreateParams
                                                                        .LineItem
                                                                        .PriceData
                                                                        .ProductData
                                                                        .builder()

                                                                        .setName(
                                                                                "Orden #"
                                                                                        + order.getId()
                                                                                        + " – "
                                                                                        + store.getNombre()
                                                                        )

                                                                        .build()
                                                        )

                                                        .build()
                                        )

                                        .build()
                        )

                        .build();

        RequestOptions options =
                RequestOptions.builder()

                        .setIdempotencyKey(
                                "store_"
                                        + store.getId()
                                        + "_order_"
                                        + order.getId()
                        )

                        .setStripeAccount(
                                store.getStripeConnectedAccountId()
                        )

                        .build();

        return Session.create(
                params,
                options
        );
    }

    public String getSessionUrl(
            String sessionId,
            Store store
    ) throws StripeException {

        validateSessionId(sessionId);

        Store connectedStore =
                requireStripeConnectedStore(store);

        Session session =
                Session.retrieve(
                        sessionId,
                        requestOptionsForStore(
                                connectedStore
                        )
                );

        if (session == null
                || session.getUrl() == null
                || session.getUrl().isBlank()) {

            throw new IllegalStateException(
                    "Sesión Stripe no válida o expirada"
            );
        }

        return session.getUrl();
    }

    public boolean isSessionExpired(
            String sessionId,
            Store store
    ) {

        validateSessionId(sessionId);

        Store connectedStore =
                requireStripeConnectedStore(store);

        try {

            Session session =
                    Session.retrieve(
                            sessionId,
                            requestOptionsForStore(
                                    connectedStore
                            )
                    );

            return session == null
                    || "expired".equalsIgnoreCase(
                            session.getStatus()
                    );

        } catch (Exception ex) {

            /*
             * Si Stripe ya no puede recuperar la sesión,
             * permitimos que el flujo genere una nueva.
             */
            return true;
        }
    }

    private Store requireStripeConnectedStore(
            Store store
    ) {

        if (store == null) {
            throw new IllegalStateException(
                    "La orden no tiene una tienda asociada"
            );
        }

        if (!store.isStripeConnected()
                || store.getStripeConnectedAccountId() == null
                || store.getStripeConnectedAccountId().isBlank()) {

            throw new IllegalStateException(
                    "La tienda debe conectar Stripe antes "
                            + "de aceptar pagos con tarjeta"
            );
        }

        return store;
    }

    private RequestOptions requestOptionsForStore(
            Store store
    ) {

        return RequestOptions.builder()
                .setStripeAccount(
                        store.getStripeConnectedAccountId()
                )
                .build();
    }

    private void validateSessionId(
            String sessionId
    ) {

        if (sessionId == null
                || sessionId.isBlank()) {

            throw new IllegalArgumentException(
                    "El sessionId es obligatorio"
            );
        }
    }

    private String resolveCurrency(
            String currency
    ) {

        if (currency == null
                || currency.isBlank()) {

            return "mxn";
        }

        return currency
                .trim()
                .toLowerCase();
    }
}