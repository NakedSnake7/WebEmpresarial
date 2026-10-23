package com.webempresarial.store.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.entity.StripeWebhookEvent;
import com.webempresarial.store.model.StripeWebhookEventStatus;
import com.webempresarial.store.repository.StripeWebhookEventRepository;

@Service
public class StripeWebhookFailureService {

    private final StripeWebhookEventRepository
            webhookEventRepository;

    public StripeWebhookFailureService(
            StripeWebhookEventRepository webhookEventRepository
    ) {
        this.webhookEventRepository =
                webhookEventRepository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void recordFailure(
            String stripeEventId,
            String eventType,
            String errorMessage
    ) {

        StripeWebhookEvent webhookEvent =
                webhookEventRepository
                        .findByStripeEventId(
                                stripeEventId
                        )
                        .orElseGet(
                                StripeWebhookEvent::new
                        );

        if (webhookEvent.getStripeEventId() == null) {
            webhookEvent.setStripeEventId(
                    stripeEventId
            );
        }

        if (webhookEvent.getEventType() == null) {
            webhookEvent.setEventType(
                    eventType
            );
        }

        webhookEvent.setStatus(
                StripeWebhookEventStatus.FAILED
        );

        webhookEvent.setProcessedAt(
                LocalDateTime.now()
        );

        webhookEvent.setErrorMessage(
                errorMessage == null
                        || errorMessage.isBlank()
                        ? "Unknown error"
                        : errorMessage
        );

        webhookEventRepository
                .saveAndFlush(webhookEvent);
    }
}
