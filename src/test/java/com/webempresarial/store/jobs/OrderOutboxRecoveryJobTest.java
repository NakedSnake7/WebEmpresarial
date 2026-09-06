package com.webempresarial.store.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxEvent;
import com.webempresarial.store.commerce.domain.order.OutboxStatus;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxRepository;
import com.webempresarial.store.commerce.infrastructure.order.scheduling.OrderOutboxRecoveryJob;



@ExtendWith(MockitoExtension.class)
class OrderOutboxRecoveryJobTest {

    @Mock
    private OrderOutboxRepository repository;

    private OrderOutboxRecoveryJob job;

    @BeforeEach
    void setUp() {
        job = new OrderOutboxRecoveryJob(
                repository,
                10L
        );
    }

    @Test
    void shouldRecoverStaleProcessingEvents() {
        OrderOutboxEvent event =
                new OrderOutboxEvent();

        event.markProcessing();

        when(repository.findByStatusAndLockedAtBefore(
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class)
        ))
        .thenReturn(List.of(event));

        job.recoverStaleEvents();

        assertThat(event.getStatus())
                .isEqualTo(OutboxStatus.FAILED);

        assertThat(event.getLockedAt())
                .isNull();

        assertThat(event.getNextAttemptAt())
                .isNotNull();

        assertThat(event.getLastError())
                .isEqualTo(
                        "Evento PROCESSING recuperado por lock expirado"
                );
    }

    @Test
    void shouldQueryOnlyProcessingEventsOlderThanConfiguredThreshold() {
        when(repository.findByStatusAndLockedAtBefore(
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class)
        ))
        .thenReturn(List.of());

        LocalDateTime before =
                LocalDateTime.now()
                        .minusMinutes(10);

        job.recoverStaleEvents();

        ArgumentCaptor<LocalDateTime> cutoffCaptor =
                ArgumentCaptor.forClass(
                        LocalDateTime.class
                );

        verify(repository)
                .findByStatusAndLockedAtBefore(
                        eq(OutboxStatus.PROCESSING),
                        cutoffCaptor.capture()
                );

        LocalDateTime cutoff =
                cutoffCaptor.getValue();

        assertThat(cutoff)
                .isAfterOrEqualTo(
                        before.minusSeconds(2)
                );

        assertThat(cutoff)
                .isBeforeOrEqualTo(
                        LocalDateTime.now()
                                .minusMinutes(10)
                                .plusSeconds(2)
                );
    }

    @Test
    void shouldDoNothingWhenThereAreNoStaleEvents() {
        when(repository.findByStatusAndLockedAtBefore(
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class)
        ))
        .thenReturn(List.of());

        job.recoverStaleEvents();

        verify(repository)
                .findByStatusAndLockedAtBefore(
                        eq(OutboxStatus.PROCESSING),
                        any(LocalDateTime.class)
                );

        verifyNoMoreInteractions(repository);
    }
}