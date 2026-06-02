package com.webempresarial.store.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.StorePlan;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeCheckoutService {

    private final StripePlanMapper stripePlanMapper;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    public StripeCheckoutService(StripePlanMapper stripePlanMapper) {
        this.stripePlanMapper = stripePlanMapper;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // =========================
    // ECOMMERCE CHECKOUT
    // =========================

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

                        .putMetadata("checkout_type", "ECOMMERCE_ORDER")
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
                                                        .setCurrency(resolveCurrency(order.getStore().getCurrency()))
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

        RequestOptions.RequestOptionsBuilder optionsBuilder = RequestOptions.builder()
                .setIdempotencyKey(
                        "store_" + order.getStore().getId() + "_order_" + order.getId()
                );

        if (order.getStore().isStripeConnected()
                && order.getStore().getStripeConnectedAccountId() != null
                && !order.getStore().getStripeConnectedAccountId().isBlank()) {

            optionsBuilder.setStripeAccount(
                    order.getStore().getStripeConnectedAccountId()
            );
        }

        RequestOptions options = optionsBuilder.build();

        return Session.create(params, options);
    }

    // =========================
    // SAAS SUBSCRIPTION CHECKOUT
    // =========================

    public Session createSaaSCheckoutSession(
            SaaSCheckoutRequestDTO dto
    ) throws StripeException {

        StorePlan plan = dto.getPlan();

        String priceId = stripePlanMapper.getPriceId(plan);

        Map<String, String> metadata = new HashMap<>();

        metadata.put("checkout_type", "SAAS_SUBSCRIPTION");
        metadata.put("companyName", dto.getCompanyName());
        metadata.put("domain", normalizeDomain(dto.getDomain()));
        metadata.put("ownerName", dto.getOwnerName());
        metadata.put("email", dto.getEmail());
        metadata.put("plan", dto.getPlan().name());
        metadata.put("env", environment);

        String baseUrl = resolvePlatformBaseUrl();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(
                                baseUrl + "/billing/success?session_id={CHECKOUT_SESSION_ID}"
                        )
                        .setCancelUrl(
                                baseUrl + "/pricing"
                        )
                        .setCustomerEmail(dto.getEmail())
                        .putAllMetadata(metadata)
                        .setClientReferenceId("SAAS-" + normalizeDomain(dto.getDomain()) + "-" + dto.getPlan().name())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(
                        "saas_" + normalizeDomain(dto.getDomain()) + "_" + dto.getPlan().name()
                )
                .build();

        return Session.create(params, options);
    }

    // =========================
    // SESSION HELPERS
    // =========================

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

    // =========================
    // PRIVATE HELPERS
    // =========================

    private String resolvePlatformBaseUrl() {
        if ("dev".equalsIgnoreCase(environment)
                || "local".equalsIgnoreCase(environment)) {
            return "http://localhost:8080";
        }

        return "https://web-empresarial.com";
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return "tenant";
        }

        return domain
                .trim()
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .replace("/", "")
                .replace(" ", "-");
    }

    private String resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "mxn";
        }

        return currency.trim().toLowerCase();
    }
}