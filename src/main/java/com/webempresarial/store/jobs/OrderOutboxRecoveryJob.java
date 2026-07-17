package com.webempresarial.store.jobs;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.OrderOutboxEvent;
import com.webempresarial.store.model.OutboxStatus;
import com.webempresarial.store.repository.OrderOutboxRepository;

@Component
public class OrderOutboxRecoveryJob {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderOutboxRecoveryJob.class
            );

    private final OrderOutboxRepository repository;
    private final long staleAfterMinutes;

    public OrderOutboxRecoveryJob(
            OrderOutboxRepository repository,
            @Value("${orders.outbox.stale-after-minutes:10}")
            long staleAfterMinutes
    ) {
        this.repository = repository;
        this.staleAfterMinutes = staleAfterMinutes;
    }

    @Scheduled(
            fixedDelayString =
                    "${orders.outbox.recovery-delay-ms:60000}"
    )
    @Transactional
    public void recoverStaleEvents() {

        LocalDateTime staleBefore =
                LocalDateTime.now()
                        .minusMinutes(staleAfterMinutes);

        List<OrderOutboxEvent> staleEvents =
                repository.findByStatusAndLockedAtBefore(
                        OutboxStatus.PROCESSING,
                        staleBefore
                );

        if (staleEvents.isEmpty()) {
            return;
        }

        for (OrderOutboxEvent event : staleEvents) {
            event.releaseStaleLock();
        }

        log.warn(
                "[Order Outbox] {} eventos atascados recuperados. "
                        + "Umbral={} minutos",
                staleEvents.size(),
                staleAfterMinutes
        );
    }
}