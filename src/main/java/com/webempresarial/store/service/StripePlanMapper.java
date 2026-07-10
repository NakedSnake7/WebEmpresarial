package com.webempresarial.store.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.webempresarial.store.model.StorePlan;

@Component
public class StripePlanMapper {

    @Value("${stripe.price.basic}")
    private String basicPriceId;

    @Value("${stripe.price.pro}")
    private String proPriceId;

    @Value("${stripe.price.premium}")
    private String premiumPriceId;

    public String getPriceId(StorePlan plan) {
        String priceId = switch (plan) {
            case BASIC -> basicPriceId;
            case PRO -> proPriceId;
            case PREMIUM -> premiumPriceId;
        };

        if (priceId == null || priceId.isBlank()) {
            throw new IllegalStateException("Stripe priceId no configurado para el plan: " + plan);
        }

        return priceId;
    }

    public boolean matches(StorePlan plan, String priceId) {
        if (plan == null || priceId == null || priceId.isBlank()) {
            return false;
        }

        return getPriceId(plan).equals(priceId);
    }
    public StorePlan getPlanByPriceId(String priceId) {

        if (priceId == null || priceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe priceId no puede estar vacío"
            );
        }

        if (basicPriceId.equals(priceId)) {
            return StorePlan.BASIC;
        }

        if (proPriceId.equals(priceId)) {
            return StorePlan.PRO;
        }

        if (premiumPriceId.equals(priceId)) {
            return StorePlan.PREMIUM;
        }

        throw new IllegalArgumentException(
                "Stripe priceId no reconocido: " + priceId
        );
    }
}