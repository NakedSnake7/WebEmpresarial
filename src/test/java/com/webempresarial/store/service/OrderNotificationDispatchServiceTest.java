package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.events.order.OrderNotificationRequestedEvent;
import com.webempresarial.store.exceptions.OrderNotFoundException;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderNotificationType;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;
import com.webempresarial.store.repository.StoreRepository;

import com.webempresarial.store.commerce.infrastructure.order.notification.OrderNotificationDispatchService;

@ExtendWith(MockitoExtension.class)
class OrderNotificationDispatchServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    private OrderNotificationDispatchService service;

    private Store store;

    @BeforeEach
    void setUp() {
        service = new OrderNotificationDispatchService(
                emailService,
                orderRepository,
                storeRepository
        );

        store = new Store();
        store.setId(1L);
    }

    @Test
    void shouldSendTransferInstructionsAndMarkAsSent()
            throws IOException {

        Order order = baseOrder();
        order.setPaymentMethod(Order.PaymentMethod.TRANSFER);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.TRANSFER_INSTRUCTIONS,
                null
        ));

        verify(emailService)
                .enviarCorreoDatosTransferencia(order);

        assertThat(order.isTransferInstructionsSent())
                .isTrue();
    }

    @Test
    void shouldNotSendTransferInstructionsTwice()
            throws IOException {

        Order order = baseOrder();
        order.setPaymentMethod(Order.PaymentMethod.TRANSFER);
        order.setTransferInstructionsSent(true);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.TRANSFER_INSTRUCTIONS,
                null
        ));

        verify(emailService, never())
                .enviarCorreoDatosTransferencia(any());
    }

    @Test
    void shouldSendPaymentConfirmationAndMarkAsSent()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.PAYMENT_CONFIRMATION,
                null
        ));

        verify(emailService)
                .enviarCorreoPedidoProcesado(
                        eq(order.getCustomerEmail()),
                        eq(order.getCustomerName()),
                        eq(order.getId()),
                        eq(order.getItems())
                );

        assertThat(order.isPaymentConfirmedSent())
                .isTrue();
    }

    @Test
    void shouldNotSendPaymentConfirmationWhenOrderIsNotProcessed()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(
                OrderStatus.PAID_PENDING_STOCK
        );

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.PAYMENT_CONFIRMATION,
                null
        ));

        verify(emailService, never())
                .enviarCorreoPedidoProcesado(
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldSendShippingConfirmationAndMarkAsSent()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber("TRACK-123");
        order.setCarrier("DHL");

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.SHIPPING_CONFIRMATION,
                null
        ));

        verify(emailService)
                .enviarCorreoEnvio(
                        eq(order.getCustomerEmail()),
                        eq(order.getCustomerName()),
                        eq(order.getId()),
                        eq(order.getOrderDate().toString()),
                        eq("TRACK-123"),
                        eq("DHL")
                );

        assertThat(order.isShippingConfirmationSent())
                .isTrue();
    }

    @Test
    void shouldNotSendShippingWithoutTracking()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.SHIPPED);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.SHIPPING_CONFIRMATION,
                null
        ));

        verify(emailService, never())
                .enviarCorreoEnvio(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldSendExpiredNotificationAndMarkAsSent()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentMethod(Order.PaymentMethod.TRANSFER);
        order.setPaymentStatus(PaymentStatus.EXPIRED);
        order.setOrderStatus(OrderStatus.CANCELLED);

        LocalDateTime expiration =
                LocalDateTime.of(
                        2026, 8, 28, 15, 0
                );

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.ORDER_EXPIRED,
                expiration
        ));

        verify(emailService)
                .enviarCorreoOrdenExpirada(
                        order,
                        expiration
                );

        assertThat(order.isOrderExpiredSent())
                .isTrue();
    }

    @Test
    void shouldUseCalculatedExpirationDateWhenEventHasNone()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentMethod(Order.PaymentMethod.TRANSFER);
        order.setPaymentStatus(PaymentStatus.EXPIRED);
        order.setOrderStatus(OrderStatus.CANCELLED);

        LocalDateTime expected =
                order.getOrderDate().plusHours(24);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.ORDER_EXPIRED,
                null
        ));

        verify(emailService)
                .enviarCorreoOrdenExpirada(
                        order,
                        expected
                );
    }

    @Test
    void shouldNotSendAnyNotificationWithoutCustomerEmail()
            throws IOException {

        Order order = baseOrder();

        order.setCustomerEmail(" ");

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        mockOrder(order);

        service.dispatch(event(
                OrderNotificationType.PAYMENT_CONFIRMATION,
                null
        ));

        verifyNoInteractions(emailService);

        assertThat(order.isPaymentConfirmedSent())
                .isFalse();
    }

    @Test
    void shouldWrapIOExceptionAndLeaveSentFlagFalse()
            throws IOException {

        Order order = baseOrder();

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);

        mockOrder(order);

        doThrow(new IOException("SMTP unavailable"))
                .when(emailService)
                .enviarCorreoPedidoProcesado(
                        any(),
                        any(),
                        any(),
                        any()
                );

        assertThatThrownBy(() ->
                service.dispatch(event(
                        OrderNotificationType.PAYMENT_CONFIRMATION,
                        null
                ))
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(
                "Error enviando notificación"
        )
        .hasCauseInstanceOf(IOException.class);

        assertThat(order.isPaymentConfirmedSent())
                .isFalse();
    }

    @Test
    void shouldRejectMissingStore() {
        when(storeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.dispatch(event(
                        OrderNotificationType.PAYMENT_CONFIRMATION,
                        null
                ))
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Tienda no encontrada: 1");

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldRejectOrderFromMissingOrWrongStore() {
        when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        when(orderRepository.findByIdFullAndStore(
                100L,
                store
        ))
        .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.dispatch(event(
                        OrderNotificationType.PAYMENT_CONFIRMATION,
                        null
                ))
        )
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Orden no encontrada: 100");

        verifyNoInteractions(emailService);
    }

    private void mockOrder(Order order) {
        when(storeRepository.findById(1L))
                .thenReturn(Optional.of(store));

        when(orderRepository.findByIdFullAndStore(
                100L,
                store
        ))
        .thenReturn(Optional.of(order));
    }

    private OrderNotificationRequestedEvent event(
            OrderNotificationType type,
            LocalDateTime expirationDate
    ) {
        return new OrderNotificationRequestedEvent(
                100L,
                1L,
                type,
                expirationDate
        );
    }

    private Order baseOrder() {
        Order order = new Order();

        order.setId(100L);
        order.setStore(store);

        order.setCustomerEmail(
                "customer@example.com"
        );

        order.setCustomerName(
                "Customer Test"
        );

        order.setOrderDate(
                LocalDateTime.of(
                        2026, 8, 27, 15, 0
                )
        );

        return order;
    }
}