package com.webempresarial.store.commerce.infrastructure.order.notification;

import java.time.LocalDateTime; 

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderNotificationType;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxRepository;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxEvent;

@Service
public class NotificationService {

    private final OrderOutboxRepository outboxRepository;

    public NotificationService(
            OrderOutboxRepository outboxRepository
    ) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void sendTransferInstructions(Order order) {
        enqueue(
                order,
                OrderNotificationType.TRANSFER_INSTRUCTIONS,
                null
        );
    }

    @Transactional
    public void sendPaymentConfirmation(Order order) {
        enqueue(
                order,
                OrderNotificationType.PAYMENT_CONFIRMATION,
                null
        );
    }

    @Transactional
    public void sendShipping(Order order) {
        enqueue(
                order,
                OrderNotificationType.SHIPPING_CONFIRMATION,
                null
        );
    }

    @Transactional
    public void sendExpired(
            Order order,
            LocalDateTime expirationDate
    ) {
        enqueue(
                order,
                OrderNotificationType.ORDER_EXPIRED,
                expirationDate
        );
    }

    private void enqueue(
            Order order,
            OrderNotificationType type,
            LocalDateTime expirationDate
    ) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException(
                    "La orden persistida es obligatoria"
            );
        }

        if (order.getStore() == null
                || order.getStore().getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda de la orden es obligatoria"
            );
        }

        String idempotencyKey =
                "ORDER:"
                        + order.getId()
                        + ":"
                        + type.name();

        outboxRepository.enqueueIgnoringDuplicate(
                order.getId(),
                order.getStore().getId(),
                type.name(),
                expirationDate,
                idempotencyKey
        );
    }

    public void notifyNewLead(Lead lead) {
        // Puede migrarse al Outbox posteriormente.
    }
}