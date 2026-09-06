package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.model.Store;

import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;

@SpringBootTest
@Transactional
class OrderRepositoryIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    private Store storeA;
    private Store storeB;

    private Order orderA;
    private Order orderB;

    @BeforeEach
    void setUp() {
        storeA = createStore(
                "Order Store A",
                "order-store-a-" + System.nanoTime() + ".local"
        );

        storeB = createStore(
                "Order Store B",
                "order-store-b-" + System.nanoTime() + ".local"
        );

        orderA = createOrder(
                storeA,
                "cliente-a@example.com",
                "stripe_session_A_" + System.nanoTime()
        );

        orderB = createOrder(
                storeB,
                "cliente-b@example.com",
                "stripe_session_B_" + System.nanoTime()
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindOrderOnlyForItsOwnStore() {
        Optional<Order> result =
                orderRepository.findByIdAndStore(
                        orderA.getId(),
                        storeA
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(orderA.getId());
    }

    @Test
    void shouldNotFindOrderFromAnotherStore() {
        Optional<Order> result =
                orderRepository.findByIdAndStore(
                        orderA.getId(),
                        storeB
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindOrderWithClienteOnlyForItsOwnStore() {
        Optional<Order> result =
                orderRepository.findByIdWithClienteAndStore(
                        orderA.getId(),
                        storeA
                );

        assertThat(result).isPresent();
    }

    @Test
    void shouldNotFindOrderWithClienteFromAnotherStore() {
        Optional<Order> result =
                orderRepository.findByIdWithClienteAndStore(
                        orderA.getId(),
                        storeB
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindFullOrderOnlyForItsOwnStore() {
        Optional<Order> result =
                orderRepository.findByIdFullAndStore(
                        orderA.getId(),
                        storeA
                );

        assertThat(result).isPresent();
    }

    @Test
    void shouldNotFindFullOrderFromAnotherStore() {
        Optional<Order> result =
                orderRepository.findByIdFullAndStore(
                        orderA.getId(),
                        storeB
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindOrderForUpdateOnlyForItsOwnStore() {
        Optional<Order> result =
                orderRepository.findByIdForUpdateAndStore(
                        orderA.getId(),
                        storeA
                );

        assertThat(result).isPresent();
    }

    @Test
    void shouldNotLockOrderFromAnotherStore() {
        Optional<Order> result =
                orderRepository.findByIdForUpdateAndStore(
                        orderA.getId(),
                        storeB
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindFullOrderForUpdateOnlyForItsOwnStore() {
        Optional<Order> result =
                orderRepository.findByIdFullForUpdateAndStore(
                        orderA.getId(),
                        storeA
                );

        assertThat(result).isPresent();
    }

    @Test
    void shouldNotLockFullOrderFromAnotherStore() {
        Optional<Order> result =
                orderRepository.findByIdFullForUpdateAndStore(
                        orderA.getId(),
                        storeB
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindStripeSessionOnlyInsideRequestedStore() {
        Optional<Order> ownStore =
                orderRepository.findByStripeSessionIdAndStore(
                        orderA.getStripeSessionId(),
                        storeA
                );

        Optional<Order> otherStore =
                orderRepository.findByStripeSessionIdAndStore(
                        orderA.getStripeSessionId(),
                        storeB
                );

        assertThat(ownStore).isPresent();
        assertThat(otherStore).isEmpty();
    }

    @Test
    void shouldReturnOnlyOrdersBelongingToRequestedStore() {
        List<Order> orders =
                orderRepository.findAllWithCliente(storeA);

        assertThat(orders)
                .extracting(Order::getId)
                .contains(orderA.getId())
                .doesNotContain(orderB.getId());
    }

    @Test
    void shouldFilterOrdersOnlyInsideRequestedStore() {
        List<Order> orders =
                orderRepository.findFilteredWithCliente(
                        OrderStatus.CREATED,
                        PaymentStatus.PENDING,
                        null,
                        null,
                        storeA
                );

        assertThat(orders)
                .extracting(Order::getId)
                .contains(orderA.getId())
                .doesNotContain(orderB.getId());
    }

    @Test
    void shouldFindPendingTransferOrdersOnlyInsideRequestedStore() {
        List<Order> orders =
                orderRepository.findPendingOrdersWithItems(
                        storeA
                );

        assertThat(orders)
                .extracting(Order::getId)
                .contains(orderA.getId())
                .doesNotContain(orderB.getId());
    }

    @Test
    void shouldCountOrdersOnlyForRequestedStore() {
        long countA =
                orderRepository.countByStoreId(
                        storeA.getId()
                );

        long countB =
                orderRepository.countByStoreId(
                        storeB.getId()
                );

        assertThat(countA).isEqualTo(1);
        assertThat(countB).isEqualTo(1);
    }

    private Store createStore(
            String name,
            String domain
    ) {
        Store store = new Store();
        store.setNombre(name);
        store.setDominio(domain);

        return storeRepository.saveAndFlush(store);
    }

    private Order createOrder(
            Store store,
            String email,
            String stripeSessionId
    ) {
        Order order = new Order();

        order.setStore(store);
        order.setCustomerName("Cliente Test");
        order.setCustomerEmail(email);
        order.setAddress("Dirección de prueba 123");
        order.setTotal(new BigDecimal("100.00"));
        order.setOrderDate(LocalDateTime.now());

        order.setOrderStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.setPaymentMethod(
                Order.PaymentMethod.TRANSFER
        );

        order.setStripeSessionId(stripeSessionId);

        return orderRepository.saveAndFlush(order);
    }
}