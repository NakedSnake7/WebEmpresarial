package com.webempresarial.store.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.infrastructure.order.scheduling.OrderExpirationScheduler;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class OrderExpirationSchedulerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private StoreRepository storeRepository;

    private OrderExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OrderExpirationScheduler(
                orderService,
                storeRepository
        );
    }

    @Test
    void shouldProcessPendingOrdersForActiveStore() {

        Store store = activeStore(1L);

        Order order1 = order(10L);
        Order order2 = order(11L);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        when(orderService.findPendingOrders(store))
                .thenReturn(List.of(order1, order2));

        when(orderService.expirarOrdenTransferencia(10L, store))
                .thenReturn(true);

        when(orderService.expirarOrdenTransferencia(11L, store))
                .thenReturn(false);

        scheduler.verificarOrdenesPendientes();

        verify(orderService)
                .findPendingOrders(store);

        verify(orderService)
                .expirarOrdenTransferencia(10L, store);

        verify(orderService)
                .expirarOrdenTransferencia(11L, store);
    }

    @Test
    void shouldIgnoreInactiveStore() {

        Store store = activeStore(1L);
        store.setActiva(false);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        scheduler.verificarOrdenesPendientes();

        verify(orderService, never())
                .findPendingOrders(any());

        verify(orderService, never())
                .expirarOrdenTransferencia(
                        anyLong(),
                        any(Store.class)
                );
    }

    @Test
    void shouldContinueWhenOneOrderFails() {

        Store store = activeStore(1L);

        Order failingOrder = order(10L);
        Order nextOrder = order(11L);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        when(orderService.findPendingOrders(store))
                .thenReturn(
                        List.of(
                                failingOrder,
                                nextOrder
                        )
                );

        when(orderService.expirarOrdenTransferencia(10L, store))
                .thenThrow(
                        new IllegalStateException(
                                "forced failure"
                        )
                );

        when(orderService.expirarOrdenTransferencia(11L, store))
                .thenReturn(true);

        scheduler.verificarOrdenesPendientes();

        verify(orderService)
                .expirarOrdenTransferencia(10L, store);

        verify(orderService)
                .expirarOrdenTransferencia(11L, store);
    }

    @Test
    void shouldNotFailWhenCandidateIsNotExpired() {

        Store store = activeStore(1L);

        Order order = order(10L);

        when(storeRepository.findAll())
                .thenReturn(List.of(store));

        when(orderService.findPendingOrders(store))
                .thenReturn(List.of(order));

        when(orderService.expirarOrdenTransferencia(10L, store))
                .thenReturn(false);

        scheduler.verificarOrdenesPendientes();

        verify(orderService)
                .expirarOrdenTransferencia(10L, store);
    }

    private Store activeStore(Long id) {

        Store store = new Store();
        store.setId(id);
        store.setActiva(true);

        return store;
    }

    private Order order(Long id) {

        Order order = new Order();
        order.setId(id);

        return order;
    }
}
