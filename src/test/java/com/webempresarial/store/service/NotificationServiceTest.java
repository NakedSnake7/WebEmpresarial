package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderNotificationType;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxRepository;
import com.webempresarial.store.commerce.infrastructure.order.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private OrderOutboxRepository repository;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository);
    }

    @Test
    void shouldEnqueueTransferInstructions() {
        Order order = persistedOrder();

        service.sendTransferInstructions(order);

        verify(repository).enqueueIgnoringDuplicate(
                10L,
                1L,
                OrderNotificationType.TRANSFER_INSTRUCTIONS.name(),
                null,
                "ORDER:10:TRANSFER_INSTRUCTIONS"
        );
    }

    @Test
    void shouldEnqueuePaymentConfirmation() {
        Order order = persistedOrder();

        service.sendPaymentConfirmation(order);

        verify(repository).enqueueIgnoringDuplicate(
                10L,
                1L,
                OrderNotificationType.PAYMENT_CONFIRMATION.name(),
                null,
                "ORDER:10:PAYMENT_CONFIRMATION"
        );
    }

    @Test
    void shouldEnqueueShippingConfirmation() {
        Order order = persistedOrder();

        service.sendShipping(order);

        verify(repository).enqueueIgnoringDuplicate(
                10L,
                1L,
                OrderNotificationType.SHIPPING_CONFIRMATION.name(),
                null,
                "ORDER:10:SHIPPING_CONFIRMATION"
        );
    }

    @Test
    void shouldEnqueueExpiredNotificationWithExpirationDate() {
        Order order = persistedOrder();

        LocalDateTime expirationDate =
                LocalDateTime.of(
                        2026, 8, 29, 16, 0
                );

        service.sendExpired(
                order,
                expirationDate
        );

        verify(repository).enqueueIgnoringDuplicate(
                10L,
                1L,
                OrderNotificationType.ORDER_EXPIRED.name(),
                expirationDate,
                "ORDER:10:ORDER_EXPIRED"
        );
    }

    @Test
    void shouldRejectNullOrder() {
        assertThatThrownBy(() ->
                service.sendPaymentConfirmation(null)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La orden persistida es obligatoria"
        );
    }

    @Test
    void shouldRejectNonPersistedOrder() {
        Order order = new Order();

        assertThatThrownBy(() ->
                service.sendPaymentConfirmation(order)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La orden persistida es obligatoria"
        );
    }

    @Test
    void shouldRejectOrderWithoutStore() {
        Order order = new Order();
        order.setId(10L);

        assertThatThrownBy(() ->
                service.sendPaymentConfirmation(order)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La tienda de la orden es obligatoria"
        );
    }

    @Test
    void shouldRejectOrderWithNonPersistedStore() {
        Order order = new Order();
        order.setId(10L);
        order.setStore(new Store());

        assertThatThrownBy(() ->
                service.sendPaymentConfirmation(order)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La tienda de la orden es obligatoria"
        );
    }

    private Order persistedOrder() {
        Store store = new Store();
        store.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setStore(store);

        return order;
    }
}