package com.webempresarial.store.jobs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webempresarial.store.service.OrderOutboxClaimService;
import com.webempresarial.store.service.OrderOutboxProcessor;

@Component
public class OrderOutboxWorker {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderOutboxWorker.class
            );

    private final OrderOutboxClaimService claimService;
    private final OrderOutboxProcessor processor;

    public OrderOutboxWorker(
            OrderOutboxClaimService claimService,
            OrderOutboxProcessor processor
    ) {
        this.claimService = claimService;
        this.processor = processor;
    }

    @Scheduled(
            fixedDelayString =
                    "${orders.outbox.fixed-delay-ms:5000}"
    )
    public void processPendingEvents() {

        List<Long> eventIds =
                claimService.claimBatch(20);

        if (eventIds.isEmpty()) {
            return;
        }

        log.debug(
                "[Order Outbox] Procesando {} eventos",
                eventIds.size()
        );

        for (Long eventId : eventIds) {
            processor.process(eventId);
        }
    }
}