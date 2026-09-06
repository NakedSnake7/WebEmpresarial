package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat; 
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.OrderTransition;
import com.webempresarial.store.commerce.domain.order.OrderTransitionContext;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderOutboxRepository;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;
import com.webempresarial.store.commerce.application.inventory.InventoryMovementService;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.application.order.OrderStateMachine;
import com.webempresarial.store.commerce.infrastructure.order.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class OrderServiceLifecycleTest {

    @Mock
    private OrderRepository orderRepository;


    @Mock
    private StockService stockService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderStateMachine orderStateMachine;

    @Mock
    private OrderOutboxRepository orderOutboxRepository;

    @Mock
    private InventoryMovementService inventoryMovementService;

    private OrderService orderService;

    private Store store;

    @BeforeEach
    void setUp() {
    	orderService = new OrderService(
    	        orderRepository,
    	        stockService,
    	        notificationService,
    	        orderStateMachine,
    	        orderOutboxRepository,
    	        inventoryMovementService
    	);

        store = new Store();
        store.setId(1L);
    }

    @Test
    void shouldCreateOrderForStore() {
        Order order = new Order();

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.crearOrden(order, store);

        assertThat(result).isSameAs(order);
        assertThat(order.getStore()).isSameAs(store);

        verify(orderRepository).save(order);
    }

    @Test
    void shouldMarkOrderAsPaid() {
        Order order = order(
                OrderStatus.CREATED,
                PaymentStatus.PENDING
        );

        when(orderRepository.findByIdForUpdateAndStore(
                10L,
                store
        )).thenReturn(Optional.of(order));

        orderService.marcarOrdenComoPagada(
                10L,
                "pi_123",
                store
        );

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.PAYMENT_CONFIRMED),
                any(OrderTransitionContext.class)
        );
    }

    @Test
    void shouldRejectPaymentForCancelledOrder() {
        Order order = order(
                OrderStatus.CANCELLED,
                PaymentStatus.PENDING
        );

        when(orderRepository.findByIdForUpdateAndStore(
                10L,
                store
        )).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.marcarOrdenComoPagada(
                        10L,
                        "pi_123",
                        store
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "No puedes pagar una orden cancelada"
        );

        verifyNoInteractions(orderStateMachine);
    }

    @Test
    void shouldIgnoreRepeatedPaymentConfirmation() {
        Order order = order(
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID
        );

        when(orderRepository.findByIdForUpdateAndStore(
                10L,
                store
        )).thenReturn(Optional.of(order));

        orderService.marcarOrdenComoPagada(
                10L,
                "pi_duplicate",
                store
        );

        verifyNoInteractions(orderStateMachine);
    }

    @Test
    void shouldRejectPostPaymentProcessingForUnpaidOrder() {
        Order order = order(
                OrderStatus.CREATED,
                PaymentStatus.PENDING
        );

        mockFullOrder(10L, order);

        assertThatThrownBy(() ->
                orderService.procesarPostPago(
                        10L,
                        store
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "No puedes procesar una orden no pagada"
        );

        verifyNoInteractions(stockService);
        verifyNoInteractions(orderStateMachine);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldProcessPaidOrderAndReduceStock() {
        Order order = order(
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID
        );

        order.setStockReduced(false);

        mockFullOrder(10L, order);

        orderService.procesarPostPago(
                10L,
                store
        );

        verify(stockService)
                .descontarStock(order, store);

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.STOCK_CONFIRMED),
                any(OrderTransitionContext.class)
        );

        verify(notificationService)
                .sendPaymentConfirmation(order);
    }

    @Test
    void shouldNotReduceStockTwiceDuringPostPaymentProcessing() {
        Order order = order(
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID
        );

        order.setStockReduced(true);

        mockFullOrder(10L, order);

        orderService.procesarPostPago(
                10L,
                store
        );

        verify(stockService, never())
                .descontarStock(any(), any());

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.STOCK_CONFIRMED),
                any(OrderTransitionContext.class)
        );

        verify(notificationService)
                .sendPaymentConfirmation(order);
    }

    @Test
    void shouldMarkStockFailureWhenReductionFails() {
        Order order = order(
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID
        );

        order.setStockReduced(false);

        mockFullOrder(10L, order);

        doThrow(new IllegalStateException(
                "Stock insuficiente"
        ))
        .when(stockService)
        .descontarStock(order, store);

        orderService.procesarPostPago(
                10L,
                store
        );

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.STOCK_FAILED),
                any(OrderTransitionContext.class)
        );

        verify(notificationService, never())
                .sendPaymentConfirmation(any());
    }

    @Test
    void shouldConfirmTransferPayment() {
        Order order = order(
                OrderStatus.CREATED,
                PaymentStatus.PENDING
        );

        order.setStockReduced(false);

        mockFullOrder(10L, order);

        orderService.confirmarPagoTransferencia(
                10L,
                store
        );

        verify(stockService)
                .descontarStock(order, store);

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.PAYMENT_CONFIRMED),
                any(OrderTransitionContext.class)
        );

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.STOCK_CONFIRMED),
                any(OrderTransitionContext.class)
        );

        verify(notificationService)
                .sendPaymentConfirmation(order);
    }

    @Test
    void shouldIgnoreAlreadyProcessedTransferPayment() {
        Order order = order(
                OrderStatus.PROCESSED,
                PaymentStatus.PAID
        );

        order.setStockReduced(true);

        mockFullOrder(10L, order);

        orderService.confirmarPagoTransferencia(
                10L,
                store
        );

        verifyNoInteractions(stockService);
        verifyNoInteractions(orderStateMachine);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldDeliverOrderThroughStatusUpdate() {
        Order order = order(
                OrderStatus.SHIPPED,
                PaymentStatus.PAID
        );

        mockFullOrder(10L, order);

        Order result =
                orderService.updateOrderStatus(
                        10L,
                        "delivered",
                        store
                );

        assertThat(result).isSameAs(order);

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.DELIVERED),
                any(OrderTransitionContext.class)
        );
    }

    @Test
    void shouldRejectUnsupportedStatusUpdate() {
        Order order = order(
                OrderStatus.PROCESSED,
                PaymentStatus.PAID
        );

        mockFullOrder(10L, order);

        assertThatThrownBy(() ->
                orderService.updateOrderStatus(
                        10L,
                        "cancelled",
                        store
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(
                "solo permite marcar"
        );

        verifyNoInteractions(orderStateMachine);
    }

    @Test
    void shouldRejectInvalidStatusValue() {
        Order order = order(
                OrderStatus.PROCESSED,
                PaymentStatus.PAID
        );

        mockFullOrder(10L, order);

        assertThatThrownBy(() ->
                orderService.updateOrderStatus(
                        10L,
                        "INVALID_STATUS",
                        store
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
                "Estado de orden no válido"
        );

        verifyNoInteractions(orderStateMachine);
    }

    @Test
    void shouldCancelUnpaidOrderAndRestoreStock() {
        Order order = order(
                OrderStatus.CREATED,
                PaymentStatus.PENDING
        );

        order.setStockReduced(true);

        mockFullOrder(10L, order);

        Order result =
                orderService.cancelOrder(
                        10L,
                        store
                );

        assertThat(result).isSameAs(order);

        verify(stockService)
                .restaurarStock(order, store);

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.CANCELLED),
                any(OrderTransitionContext.class)
        );
    }

    @Test
    void shouldRejectCancellationOfPaidOrder() {
        Order order = order(
                OrderStatus.PAID_PENDING_STOCK,
                PaymentStatus.PAID
        );

        mockFullOrder(10L, order);

        assertThatThrownBy(() ->
                orderService.cancelOrder(
                        10L,
                        store
                )
        )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
                "Una orden pagada no puede cancelarse directamente"
        );

        verifyNoInteractions(stockService);
        verifyNoInteractions(orderStateMachine);
    }

    @Test
    void shouldExpireTransferOrderAndRestoreStock() {
        Order order = order(
                OrderStatus.CREATED,
                PaymentStatus.PENDING
        );

        order.setPaymentMethod(
                Order.PaymentMethod.TRANSFER
        );

        order.setOrderDate(
                LocalDateTime.now().minusHours(25)
        );

        order.setStockReduced(true);

        mockFullOrder(10L, order);

        boolean expired =
                orderService.expirarOrdenTransferencia(
                        10L,
                        store
                );

        assertThat(expired).isTrue();

        verify(stockService)
                .restaurarStock(order, store);

        verify(orderStateMachine).transition(
                eq(order),
                eq(OrderTransition.EXPIRED),
                any(OrderTransitionContext.class)
        );

        verify(notificationService).sendExpired(
                eq(order),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldNotExpireIneligibleOrder() {
        Order order = order(
                OrderStatus.PROCESSED,
                PaymentStatus.PAID
        );

        mockFullOrder(10L, order);

        boolean expired =
                orderService.expirarOrdenTransferencia(
                        10L,
                        store
                );

        assertThat(expired).isFalse();

        verifyNoInteractions(stockService);
        verifyNoInteractions(orderStateMachine);
        verifyNoInteractions(notificationService);
    }

    private Order order(
            OrderStatus orderStatus,
            PaymentStatus paymentStatus
    ) {
        Order order = new Order();
        order.setId(10L);
        order.setStore(store);
        order.setOrderStatus(orderStatus);
        order.setPaymentStatus(paymentStatus);
        return order;
    }

    private void mockFullOrder(
            Long orderId,
            Order order
    ) {
        when(orderRepository
                .findByIdFullForUpdateAndStore(
                        orderId,
                        store
                ))
                .thenReturn(Optional.of(order));
    }
}