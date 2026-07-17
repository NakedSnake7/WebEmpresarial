package com.webempresarial.store.model;

public enum OrderAuditAction {

    ORDER_CREATED,
    PAYMENT_CONFIRMED,
    STOCK_CONFIRMED,
    STOCK_FAILED,
    SHIPPING_UPDATED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    ORDER_EXPIRED
}