package com.webempresarial.store.commerce.domain.order;

public enum OrderStatus {
    CREATED,     // orden creada
    PROCESSED,   // preparada
    SHIPPED,     // enviada
    DELIVERED,   // entregada
    CANCELLED    // cancelada
, PAID_PENDING_STOCK
}
