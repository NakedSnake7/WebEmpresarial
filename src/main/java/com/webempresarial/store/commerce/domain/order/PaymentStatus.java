package com.webempresarial.store.commerce.domain.order;

public enum PaymentStatus {
    PENDING,     // esperando pago (Stripe / transferencia)
    PAID,        // pago confirmado
    FAILED,      // pago fallido
    EXPIRED      // pago nunca llegó
}
