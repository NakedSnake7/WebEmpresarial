package com.webempresarial.store.commerce.domain.order;

public enum OrderTransition {

    PAYMENT_CONFIRMED,
    STOCK_CONFIRMED,
    STOCK_FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    EXPIRED
}