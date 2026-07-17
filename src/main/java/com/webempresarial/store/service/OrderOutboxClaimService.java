package com.webempresarial.store.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.OrderOutboxEvent;
import com.webempresarial.store.repository.OrderOutboxRepository;

@Service
public class OrderOutboxClaimService {

    private final OrderOutboxRepository repository;

    public OrderOutboxClaimService(
            OrderOutboxRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public List<Long> claimBatch(int batchSize) {

        List<OrderOutboxEvent> events =
                repository.findClaimableForUpdate(
                        LocalDateTime.now(),
                        PageRequest.of(0, batchSize)
                );

        for (OrderOutboxEvent event : events) {
            event.markProcessing();
        }

        return events.stream()
                .map(OrderOutboxEvent::getId)
                .toList();
    }

    @Transactional
    public void markProcessed(Long eventId) {

        OrderOutboxEvent event =
                repository.findByIdForUpdate(eventId);

        if (event == null) {
            return;
        }

        event.markProcessed();
    }

    @Transactional
    public void markFailed(
            Long eventId,
            Throwable throwable
    ) {
        OrderOutboxEvent event =
                repository.findByIdForUpdate(eventId);

        if (event == null) {
            return;
        }

        long delayMinutes = calculateBackoffMinutes(
                event.getAttempts()
        );

        LocalDateTime retryAt =
                LocalDateTime.now().plusMinutes(
                        delayMinutes
                );

        String error =
                throwable != null
                        ? throwable.getClass().getSimpleName()
                            + ": "
                            + throwable.getMessage()
                        : "Error desconocido";

        event.markFailed(error, retryAt);
    }

    private long calculateBackoffMinutes(int attempts) {
        long exponential =
                1L << Math.min(
                        Math.max(attempts - 1, 0),
                        6
                );

        return Math.min(exponential, 60L);
    }
}