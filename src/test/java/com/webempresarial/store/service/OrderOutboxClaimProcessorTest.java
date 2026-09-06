package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxEvent;
import com.webempresarial.store.events.order.OrderNotificationRequestedEvent;
import com.webempresarial.store.commerce.domain.order.OrderNotificationType;
import com.webempresarial.store.commerce.domain.order.OutboxStatus;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxRepository;

import com.webempresarial.store.commerce.infrastructure.order.outbox.OrderOutboxClaimService;
import com.webempresarial.store.commerce.infrastructure.order.outbox.OrderOutboxProcessor;
import com.webempresarial.store.commerce.infrastructure.order.notification.OrderNotificationDispatchService;
@ExtendWith(MockitoExtension.class)
class OrderOutboxClaimProcessorTest {

    @Mock
    private OrderOutboxRepository repository;

    @Mock
    private OrderNotificationDispatchService dispatchService;

    @Mock
    private OrderOutboxClaimService claimService;

    private OrderOutboxClaimService realClaimService;
    private OrderOutboxProcessor processor;

    @BeforeEach
    void setUp() {
        realClaimService =
                new OrderOutboxClaimService(repository);

        processor =
                new OrderOutboxProcessor(
                        repository,
                        dispatchService,
                        claimService
                );
    }

    @Test
    void shouldClaimAvailableEvents() {
        OrderOutboxEvent first = event(1L);
        OrderOutboxEvent second = event(2L);

        when(repository.findClaimableForUpdate(
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(first, second));

        List<Long> ids =
                realClaimService.claimBatch(20);

        assertThat(ids)
                .containsExactly(1L, 2L);

        assertThat(first.getStatus())
                .isEqualTo(OutboxStatus.PROCESSING);

        assertThat(second.getStatus())
                .isEqualTo(OutboxStatus.PROCESSING);

        assertThat(first.getAttempts())
                .isEqualTo(1);

        assertThat(second.getAttempts())
                .isEqualTo(1);

        assertThat(first.getLockedAt())
                .isNotNull();

        assertThat(second.getLockedAt())
                .isNotNull();
    }

    @Test
    void shouldReturnEmptyBatchWhenNothingIsClaimable() {
        when(repository.findClaimableForUpdate(
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of());

        List<Long> ids =
                realClaimService.claimBatch(20);

        assertThat(ids).isEmpty();
    }

    @Test
    void shouldMarkEventProcessed() {
        OrderOutboxEvent event = event(10L);

        event.markProcessing();

        when(repository.findByIdForUpdate(10L))
                .thenReturn(event);

        realClaimService.markProcessed(10L);

        assertThat(event.getStatus())
                .isEqualTo(OutboxStatus.PROCESSED);

        assertThat(event.getProcessedAt())
                .isNotNull();

        assertThat(event.getLockedAt())
                .isNull();

        assertThat(event.getNextAttemptAt())
                .isNull();

        assertThat(event.getLastError())
                .isNull();
    }

    @Test
    void shouldIgnoreMarkProcessedWhenEventDoesNotExist() {
        when(repository.findByIdForUpdate(10L))
                .thenReturn(null);

        realClaimService.markProcessed(10L);

        verify(repository)
                .findByIdForUpdate(10L);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldMarkEventFailedAndScheduleRetry() {
        OrderOutboxEvent event = event(10L);

        event.markProcessing();

        when(repository.findByIdForUpdate(10L))
                .thenReturn(event);

        LocalDateTime before =
                LocalDateTime.now();

        realClaimService.markFailed(
                10L,
                new IllegalStateException(
                        "SMTP unavailable"
                )
        );

        assertThat(event.getStatus())
                .isEqualTo(OutboxStatus.FAILED);

        assertThat(event.getLockedAt())
                .isNull();

        assertThat(event.getLastError())
                .contains("IllegalStateException")
                .contains("SMTP unavailable");

        assertThat(event.getNextAttemptAt())
                .isAfterOrEqualTo(
                        before.plusMinutes(1)
                );
    }

    @Test
    void shouldIncreaseRetryBackoffWithAttempts() {
        OrderOutboxEvent event = event(10L);

        // attempts = 1
        event.markProcessing();

        // Simulamos nuevos intentos previos.
        event.markFailed(
                "previous failure",
                LocalDateTime.now()
        );

        // attempts = 2
        event.markProcessing();

        // attempts = 3
        event.markFailed(
                "previous failure",
                LocalDateTime.now()
        );
        event.markProcessing();

        when(repository.findByIdForUpdate(10L))
                .thenReturn(event);

        LocalDateTime before =
                LocalDateTime.now();

        realClaimService.markFailed(
                10L,
                new RuntimeException("failure")
        );

        /*
         * attempts = 3
         * backoff = 2^(3-1) = 4 minutos
         */
        assertThat(event.getNextAttemptAt())
                .isAfterOrEqualTo(
                        before.plusMinutes(4)
                );
    }

    @Test
    void shouldCapRetryBackoffAtSixtyMinutes() {
        OrderOutboxEvent event = event(10L);

        for (int i = 0; i < 10; i++) {
            event.markProcessing();
            event.markFailed(
                    "failure",
                    LocalDateTime.now()
            );
        }

        when(repository.findByIdForUpdate(10L))
                .thenReturn(event);

        LocalDateTime before =
                LocalDateTime.now();

        realClaimService.markFailed(
                10L,
                new RuntimeException("failure")
        );

        assertThat(event.getNextAttemptAt())
                .isAfterOrEqualTo(
                        before.plusMinutes(60)
                );

        assertThat(event.getNextAttemptAt())
                .isBefore(
                        before.plusMinutes(61)
                );
    }

    @Test
    void shouldIgnoreMarkFailedWhenEventDoesNotExist() {
        when(repository.findByIdForUpdate(10L))
                .thenReturn(null);

        realClaimService.markFailed(
                10L,
                new RuntimeException("failure")
        );

        verify(repository)
                .findByIdForUpdate(10L);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldDispatchEventAndMarkItProcessed() {
        OrderOutboxEvent event = event(10L);

        when(repository.findById(10L))
                .thenReturn(Optional.of(event));

        processor.process(10L);

        ArgumentCaptor<OrderNotificationRequestedEvent> captor =
                ArgumentCaptor.forClass(
                        OrderNotificationRequestedEvent.class
                );

        verify(dispatchService)
                .dispatch(captor.capture());

        OrderNotificationRequestedEvent dispatched =
                captor.getValue();

        assertThat(dispatched.orderId())
                .isEqualTo(100L);

        assertThat(dispatched.storeId())
                .isEqualTo(1L);

        assertThat(dispatched.type())
                .isEqualTo(
                        OrderNotificationType.PAYMENT_CONFIRMATION
                );

        verify(claimService)
                .markProcessed(10L);

        verify(claimService, never())
                .markFailed(any(), any());
    }

    @Test
    void shouldMarkEventFailedWhenDispatchThrows() {
        OrderOutboxEvent event = event(10L);

        when(repository.findById(10L))
                .thenReturn(Optional.of(event));

        IllegalStateException failure =
                new IllegalStateException(
                        "SMTP error"
                );

        doThrow(failure)
                .when(dispatchService)
                .dispatch(any());

        processor.process(10L);

        verify(claimService, never())
                .markProcessed(any());

        verify(claimService)
                .markFailed(10L, failure);
    }

    @Test
    void shouldIgnoreMissingEventDuringProcessing() {
        when(repository.findById(10L))
                .thenReturn(Optional.empty());

        processor.process(10L);

        verifyNoInteractions(dispatchService);
        verifyNoInteractions(claimService);
    }

    private OrderOutboxEvent event(Long id) {
        OrderOutboxEvent event =
                new OrderOutboxEvent();

        /*
         * OrderOutboxEvent no expone setId().
         * Para los tests de claim/process necesitamos un id,
         * así que usamos reflexión únicamente en el fixture.
         */
        setId(event, id);

        event.setOrderId(100L);
        event.setStoreId(1L);

        event.setEventType(
                OrderNotificationType.PAYMENT_CONFIRMATION
        );

        event.setIdempotencyKey(
                "ORDER:100:PAYMENT_CONFIRMATION"
        );

        return event;
    }

    private void setId(
            OrderOutboxEvent event,
            Long id
    ) {
        try {
            var field =
                    OrderOutboxEvent.class
                            .getDeclaredField("id");

            field.setAccessible(true);
            field.set(event, id);

        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(
                    "No se pudo asignar id al fixture",
                    ex
            );
        }
    }
}