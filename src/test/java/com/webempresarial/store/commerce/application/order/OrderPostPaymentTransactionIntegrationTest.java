package com.webempresarial.store.commerce.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovement;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.OrderItem;
import com.webempresarial.store.commerce.domain.order.OrderStatus;
import com.webempresarial.store.commerce.domain.order.PaymentStatus;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;
import com.webempresarial.store.commerce.infrastructure.order.persistence.OrderRepository;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.CategoriaRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.StoreRepository;

@SpringBootTest
class OrderPostPaymentTransactionIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    /*
     * Deliberadamente mockeamos únicamente el último
     * colaborador del flujo de stock.
     *
     * Todo lo anterior —locks, Producto, save, movimiento,
     * JPA y transacciones— continúa siendo real.
     */
    @MockBean
    private InventoryPersistentAlertService persistentAlertService;

    private Store store;
    private Categoria categoria;
    private Producto producto;
    private Order order;

    @BeforeEach
    void setUp() {

        store = createStore();

        categoria = createCategory(
                store,
                "Categoría rollback"
        );

        producto = createProduct(
                store,
                categoria,
                "Producto rollback",
                10
        );

        order = createPaidOrder(
                store,
                producto,
                4
        );
    }

    @AfterEach
    void tearDown() {

        if (order != null && order.getId() != null) {

            List<InventoryMovement> movements =
                    movementRepository
                            .findByOrderIdAndStoreOrderByCreatedAtAsc(
                                    order.getId(),
                                    store
                            );

            if (!movements.isEmpty()) {
                movementRepository.deleteAll(movements);
                movementRepository.flush();
            }

            orderRepository
                    .findById(order.getId())
                    .ifPresent(existing -> {
                        orderRepository.delete(existing);
                        orderRepository.flush();
                    });
        }

        if (producto != null
                && producto.getId() != null) {

            productoRepository
                    .findById(producto.getId())
                    .ifPresent(existing -> {
                        productoRepository.delete(existing);
                        productoRepository.flush();
                    });
        }

        if (categoria != null
                && categoria.getId() != null) {

            categoriaRepository
                    .findById(categoria.getId())
                    .ifPresent(existing -> {
                        categoriaRepository.delete(existing);
                        categoriaRepository.flush();
                    });
        }

        if (store != null
                && store.getId() != null) {

            storeRepository
                    .findById(store.getId())
                    .ifPresent(existing -> {
                        storeRepository.delete(existing);
                        storeRepository.flush();
                    });
        }
    }

    @Test
    void shouldRollbackInventoryAndKeepPaidOrderPendingStockWhenPostPaymentFails() {

        /*
         * StockService hace:
         *
         * 1. SELECT FOR UPDATE
         * 2. stock 10 -> 6
         * 3. save product
         * 4. save InventoryMovement
         * 5. evaluateSimpleProduct()
         *
         * Fallamos deliberadamente en el paso 5.
         */
        doThrow(
                new IllegalStateException(
                        "Fallo deliberado después de modificar stock"
                )
        )
                .when(persistentAlertService)
                .evaluateSimpleProduct(
                        any(Producto.class),
                        eq(store)
                );

        orderService.procesarPostPago(
                order.getId(),
                store
        );

        /*
         * ====================================================
         * RECARGAR DESDE BASE DE DATOS
         * ====================================================
         *
         * No comprobamos los objetos del fixture en memoria.
         * Queremos conocer lo que realmente quedó committed.
         */

        Producto persistedProduct =
                productoRepository
                        .findById(producto.getId())
                        .orElseThrow();

        Order persistedOrder =
                orderRepository
                        .findByIdFullAndStore(
                                order.getId(),
                                store
                        )
                        .orElseThrow();

        List<InventoryMovement> persistedMovements =
                movementRepository
                        .findByOrderIdAndStoreOrderByCreatedAtAsc(
                                order.getId(),
                                store
                        );

        /*
         * CRÍTICO:
         *
         * El producto fue modificado 10 -> 6 antes
         * de producirse la excepción.
         *
         * Si la nueva frontera transaccional funciona,
         * esa modificación debe haber hecho rollback.
         */
        assertThat(
                persistedProduct.getStockSimple()
        ).isEqualTo(10);

        /*
         * El movimiento SALE también fue creado antes
         * de la excepción. Debe desaparecer con el mismo
         * rollback.
         */
        assertThat(
                persistedMovements
        ).isEmpty();

        /*
         * El pago ocurrió anteriormente en otra
         * transacción REQUIRES_NEW.
         *
         * Un fallo logístico jamás debe borrar
         * la realidad financiera.
         */
        assertThat(
                persistedOrder.getPaymentStatus()
        ).isEqualTo(
                PaymentStatus.PAID
        );

        /*
         * No conseguimos reservar/descontar inventario.
         */
        assertThat(
                persistedOrder.isStockReduced()
        ).isFalse();

        /*
         * La orden continúa esperando resolución de stock.
         */
        assertThat(
                persistedOrder.getOrderStatus()
        ).isEqualTo(
                OrderStatus.PAID_PENDING_STOCK
        );
    }

    private Store createStore() {

        Store store = new Store();

        store.setNombre(
                "Post Payment Integration "
                        + System.nanoTime()
        );

        store.setDominio(
                "post-payment-"
                        + System.nanoTime()
                        + ".local"
        );

        return storeRepository
                .saveAndFlush(store);
    }

    private Categoria createCategory(
            Store store,
            String name
    ) {

        Categoria categoria =
                new Categoria();

        categoria.setNombre(name);
        categoria.setStore(store);

        return categoriaRepository
                .saveAndFlush(categoria);
    }

    private Producto createProduct(
            Store store,
            Categoria categoria,
            String name,
            int stock
    ) {

        Producto producto =
                new Producto();

        producto.setStore(store);
        producto.setProductName(name);

        producto.setPrice(
                new BigDecimal("100.00")
        );

        producto.setStockSimple(stock);
        producto.setCategoria(categoria);

        producto.setVisibleEnMenu(true);
        producto.setTienePromocion(false);
        producto.setPorcentajeDescuento(0.0);

        return productoRepository
                .saveAndFlush(producto);
    }

    private Order createPaidOrder(
            Store store,
            Producto producto,
            int quantity
    ) {

        Order order = new Order();

        order.setStore(store);

        order.setCustomerName(
                "Cliente Integration Test"
        );

        order.setCustomerEmail(
                "post-payment-"
                        + System.nanoTime()
                        + "@example.com"
        );

        order.setAddress(
                "Dirección integration test 123"
        );

        order.setTotal(
                BigDecimal.valueOf(quantity)
                        .multiply(
                                producto.getPrice()
                        )
        );

        order.setOrderDate(
                LocalDateTime.now()
        );

        order.setOrderStatus(
                OrderStatus.PAID_PENDING_STOCK
        );

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        order.setPaymentMethod(
                Order.PaymentMethod.STRIPE
        );

        order.setStockReduced(false);

        OrderItem item =
                new OrderItem(
                        producto,
                        quantity,
                        producto.getPrice(),
                        order
                );

        item.setProductName(
                producto.getProductName()
        );

        /*
         * CascadeType.ALL está en Order.items,
         * pero debemos mantener ambos lados
         * de la asociación.
         */
        order.getItems().add(item);

        return orderRepository
                .saveAndFlush(order);
    }
}