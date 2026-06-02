package com.webempresarial.store.service;

import org.springframework.stereotype.Component;

import com.webempresarial.store.model.StorePlan;

@Component
public class StripePlanMapper {

    public String getPriceId(StorePlan plan) {

        return switch (plan) {
            case BASIC -> "price_BASIC_ID";
            case PRO -> "price_PRO_ID";
            case PREMIUM -> "price_PREMIUM_ID";
        };
    }
}