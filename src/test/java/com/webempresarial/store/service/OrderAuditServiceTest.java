package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderAuditLog;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderAuditLogRepository;

import com.webempresarial.store.commerce.application.order.OrderAuditService;

@ExtendWith(MockitoExtension.class)
class OrderAuditServiceTest {

    @Mock
    private OrderAuditLogRepository repository;

    private OrderAuditService service;

    @BeforeEach
    void setUp() {
        service = new OrderAuditService(repository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void findTimeline_shouldReturnAuditLogsForOrderAndStore() {
        Long orderId = 100L;
        Long storeId = 10L;

        OrderAuditLog first = mock(OrderAuditLog.class);
        OrderAuditLog second = mock(OrderAuditLog.class);

        List<OrderAuditLog> expected = List.of(first, second);

        when(repository.findByOrderIdAndStoreIdOrderByCreatedAtAsc(orderId, storeId))
                .thenReturn(expected);

        List<OrderAuditLog> result =
                service.findTimeline(orderId, storeId);

        assertThat(result).isEqualTo(expected);

        verify(repository)
                .findByOrderIdAndStoreIdOrderByCreatedAtAsc(orderId, storeId);
    }
    
    @Test
    void findTimeline_shouldRejectNullOrderId() {
        assertThatThrownBy(() ->
                service.findTimeline(null, 10L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId y storeId son obligatorios");

        verifyNoInteractions(repository);
    }

    @Test
    void findTimeline_shouldRejectNullStoreId() {
        assertThatThrownBy(() ->
                service.findTimeline(100L, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderId y storeId son obligatorios");

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRecordAuditAsSystemWhenThereIsNoAuthentication() {
        Order order = persistedOrder();

        service.record(
                order,
                OrderAuditAction.PAYMENT_CONFIRMED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Pago confirmado"
        );

        ArgumentCaptor<OrderAuditLog> captor =
                ArgumentCaptor.forClass(OrderAuditLog.class);

        verify(repository).save(captor.capture());

        OrderAuditLog audit = captor.getValue();

        assertThat(audit.getOrderId()).isEqualTo(10L);
        assertThat(audit.getStoreId()).isEqualTo(1L);
        assertThat(audit.getAction())
                .isEqualTo(OrderAuditAction.PAYMENT_CONFIRMED);

        assertThat(audit.getPreviousOrderStatus())
                .isEqualTo(OrderStatus.CREATED);

        assertThat(audit.getNewOrderStatus())
                .isEqualTo(OrderStatus.PAID_PENDING_STOCK);

        assertThat(audit.getPreviousPaymentStatus())
                .isEqualTo(PaymentStatus.PENDING);

        assertThat(audit.getNewPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(audit.getActorUsername())
                .isEqualTo("SYSTEM");

        assertThat(audit.getActorType())
                .isEqualTo("SYSTEM");

        assertThat(audit.getReason())
                .isEqualTo("Pago confirmado");
    }

    @Test
    void shouldRecordAuthenticatedActor() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        "secret",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                )
        );

        Order order = persistedOrder();

        service.record(
                order,
                OrderAuditAction.PAYMENT_CONFIRMED,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                "Pago confirmado"
        );

        ArgumentCaptor<OrderAuditLog> captor =
                ArgumentCaptor.forClass(OrderAuditLog.class);

        verify(repository).save(captor.capture());

        OrderAuditLog audit = captor.getValue();

        assertThat(audit.getActorUsername())
                .isEqualTo("admin@test.com");

        assertThat(audit.getActorType())
                .isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldRejectNullOrder() {
        assertThatThrownBy(() ->
                service.record(
                        null,
                        OrderAuditAction.PAYMENT_CONFIRMED,
                        OrderStatus.CREATED,
                        PaymentStatus.PENDING,
                        "test"
                )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
                "La orden persistida y su tienda son obligatorias"
        );
    }

    @Test
    void shouldRejectNonPersistedOrder() {
        Order order = new Order();

        Store store = new Store();
        store.setId(1L);

        order.setStore(store);

        assertThatThrownBy(() ->
                service.record(
                        order,
                        OrderAuditAction.PAYMENT_CONFIRMED,
                        OrderStatus.CREATED,
                        PaymentStatus.PENDING,
                        "test"
                )
        )
        .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectOrderWithoutPersistedStore() {
        Order order = new Order();
        order.setId(10L);
        order.setStore(new Store());

        assertThatThrownBy(() ->
                service.record(
                        order,
                        OrderAuditAction.PAYMENT_CONFIRMED,
                        OrderStatus.CREATED,
                        PaymentStatus.PENDING,
                        "test"
                )
        )
        .isInstanceOf(IllegalArgumentException.class);
    }

    private Order persistedOrder() {
        Store store = new Store();
        store.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setStore(store);

        order.setOrderStatus(
                OrderStatus.PAID_PENDING_STOCK
        );

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        return order;
    }
}