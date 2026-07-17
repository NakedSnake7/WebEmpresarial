package com.webempresarial.store.events.order;

import java.time.LocalDateTime;

import com.webempresarial.store.model.OrderNotificationType;

public record OrderNotificationRequestedEvent(
        Long orderId,
        Long storeId,
        OrderNotificationType type,
        LocalDateTime expirationDate
) {
}