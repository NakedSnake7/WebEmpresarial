package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.webempresarial.store.dto.billing.SaaSCheckoutRequestDTO;
import com.webempresarial.store.model.StorePlan;

import jakarta.annotation.PostConstruct;

@Service
public class StripeSaaSCheckoutService {
	
    private final StripePlanMapper stripePlanMapper;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.environment:prod}")
    private String environment;

    public StripeSaaSCheckoutService(
            StripePlanMapper stripePlanMapper
    )  {
        this.stripePlanMapper =
                stripePlanMapper;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createSaaSCheckoutSession(
            SaaSCheckoutRequestDTO dto,
            String checkoutAttemptId,
            LocalDateTime expiresAt
    ) throws StripeException {

        if (checkoutAttemptId == null
                || checkoutAttemptId.isBlank()) {

            throw new IllegalArgumentException(
                    "checkoutAttemptId es obligatorio"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "expiresAt es obligatorio"
            );
        }

        StorePlan plan =
                dto.getPlan();

        String priceId =
                stripePlanMapper.getPriceId(plan);

        Map<String, String> metadata =
                new HashMap<>();

        metadata.put(
                "checkout_type",
                "SAAS_SUBSCRIPTION"
        );

        metadata.put(
                "companyName",
                dto.getCompanyName()
        );

        metadata.put(
                "domain",
                normalizeDomain(dto.getDomain())
        );

        metadata.put(
                "ownerName",
                dto.getOwnerName()
        );

        metadata.put(
                "email",
                dto.getEmail()
        );

        metadata.put(
                "plan",
                dto.getPlan().name()
        );

        metadata.put(
                "stripe_price_id",
                priceId
        );

        metadata.put(
                "env",
                environment
        );

        metadata.put(
                "checkout_attempt_id",
                checkoutAttemptId
        );

        String baseUrl =
                resolvePlatformBaseUrl();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(
                                SessionCreateParams.Mode.SUBSCRIPTION
                        )
                        .setSuccessUrl(
                                baseUrl
                                        + "/billing/success?session_id={CHECKOUT_SESSION_ID}"
                        )
                        .setCancelUrl(
                                baseUrl + "/#saas-checkout"
                        )
                        .setCustomerEmail(
                                dto.getEmail()
                        )
                        .putAllMetadata(metadata)
                        .setClientReferenceId(
                                "SAAS-" + checkoutAttemptId
                        )
                        .setExpiresAt(
                                expiresAt
                                        .atZone(
                                                ZoneId.systemDefault()
                                        )
                                        .toEpochSecond()
                        )
                        .addLineItem(
                                SessionCreateParams.LineItem
                                        .builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .build();

        RequestOptions options =
                RequestOptions.builder()
                        .setIdempotencyKey(
                                "saas_checkout_"
                                        + checkoutAttemptId
                        )
                        .build();

        return Session.create(
                params,
                options
        );
    }

    private String resolvePlatformBaseUrl() {

        if ("dev".equalsIgnoreCase(environment)
                || "local".equalsIgnoreCase(
                        environment
                )) {

            return "http://localhost:8080";
        }

        return "https://web-empresarial.com";
    }

    private String normalizeDomain(
            String domain
    ) {

        if (domain == null
                || domain.isBlank()) {

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
}