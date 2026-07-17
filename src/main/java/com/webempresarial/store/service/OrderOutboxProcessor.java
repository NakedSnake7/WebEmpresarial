package com.webempresarial.store.service;

import org.springframework.stereotype.Service;

import com.webempresarial.store.entity.OrderOutboxEvent;
import com.webempresarial.store.events.order.OrderNotificationRequestedEvent;
import com.webempresarial.store.repository.OrderOutboxRepository;

@Service
public class OrderOutboxProcessor {

    private final OrderOutboxRepository repository;
    private final OrderNotificationDispatchService dispatchService;
    private final OrderOutboxClaimService claimService;

    public OrderOutboxProcessor(
            OrderOutboxRepository repository,
            OrderNotificationDispatchService dispatchService,
            OrderOutboxClaimService claimService
    ) {
        this.repository = repository;
        this.dispatchService = dispatchService;
        this.claimService = claimService;
    }

    public void process(Long eventId) {

        OrderOutboxEvent outboxEvent =
                repository.findById(eventId)
                        .orElse(null);

        if (outboxEvent == null) {
            return;
        }

        try {
            dispatchService.dispatch(
                    new OrderNotificationRequestedEvent(
                            outboxEvent.getOrderId(),
                            outboxEvent.getStoreId(),
                            outboxEvent.getEventType(),
                            outboxEvent.getExpirationDate()
                    )
            );

            claimService.markProcessed(eventId);

        } catch (Exception ex) {
            claimService.markFailed(eventId, ex);
        }
    }
}