package com.webempresarial.store.commerce.domain.order;

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