package com.webempresarial.store.model;

public enum OrderTransition {

    PAYMENT_CONFIRMED,
    STOCK_CONFIRMED,
    STOCK_FAILED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    EXPIRED
}