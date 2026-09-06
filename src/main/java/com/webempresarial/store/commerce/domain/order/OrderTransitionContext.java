package com.webempresarial.store.commerce.domain.order;

public record OrderTransitionContext(
        String paymentIntentId,
        String trackingNumber,
        String carrier
) {

    public static OrderTransitionContext empty() {
        return new OrderTransitionContext(
                null,
                null,
                null
        );
    }

    public static OrderTransitionContext payment(
            String paymentIntentId
    ) {
        return new OrderTransitionContext(
                paymentIntentId,
                null,
                null
        );
    }

    public static OrderTransitionContext shipping(
            String trackingNumber,
            String carrier
    ) {
        return new OrderTransitionContext(
                null,
                trackingNumber,
                carrier
        );
    }
}