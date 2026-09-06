package com.webempresarial.store.service;

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
            SaaSCheckoutRequestDTO dto
    ) throws StripeException {

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
                                baseUrl + "/pricing"
                        )
                        .setCustomerEmail(
                                dto.getEmail()
                        )
                        .putAllMetadata(metadata)
                        .setClientReferenceId(
                                "SAAS-"
                                        + normalizeDomain(
                                                dto.getDomain()
                                        )
                                        + "-"
                                        + dto.getPlan().name()
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
                                "saas_"
                                        + normalizeDomain(
                                                dto.getDomain()
                                        )
                                        + "_"
                                        + dto.getPlan().name()
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