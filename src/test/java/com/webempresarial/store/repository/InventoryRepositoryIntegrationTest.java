package com.webempresarial.store.repository;

import static org.assertj.core.api.Assertions.*; 

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.webempresarial.store.commerce.domain.inventory.InventoryAlert;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovement;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertLevel;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertStatus;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryAlertRepository;
import com.webempresarial.store.commerce.infrastructure.inventory.persistence.InventoryMovementRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class InventoryRepositoryIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private InventoryAlertRepository alertRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private InventoryPersistentAlertService alertService;

    @Autowired
    private EntityManager entityManager;

    /*
     * =========================================================
     * PRODUCT MULTI-TENANCY
     * =========================================================
     */

    @Test
    void shouldNotFindProductFromAnotherStoreUsingPessimisticLock() {

        Store storeA =
                createStore(
                        "Inventory Store A",
                        "inventory-a.local"
                );

        Store storeB =
                createStore(
                        "Inventory Store B",
                        "inventory-b.local"
                );

        Producto productoA =
                createProduct(
                        storeA,
                        "Producto A",
                        10
                );

        flushAndClear();

        Optional<Producto> result =
                productoRepository.findByIdForUpdate(
                        productoA.getId(),
                        storeB
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldFindProductOnlyForItsOwnStoreUsingPessimisticLock() {

        Store store =
                createStore(
                        "Inventory Product Owner",
                        "inventory-product-owner.local"
                );

        Producto producto =
                createProduct(
                        store,
                        "Producto protegido",
                        10
                );

        flushAndClear();

        Optional<Producto> result =
                productoRepository.findByIdForUpdate(
                        producto.getId(),
                        store
                );

        assertThat(result)
                .isPresent();

        assertThat(result.get().getId())
                .isEqualTo(producto.getId());

        assertThat(result.get()
                .getStore()
                .getId())
                .isEqualTo(store.getId());
    }

    /*
     * =========================================================
     * VARIANT MULTI-TENANCY
     * =========================================================
     */

    @Test
    void shouldNotFindVariantFromAnotherStoreUsingPessimisticLock() {

        Store storeA =
                createStore(
                        "Variant Store A",
                        "inventory-variant-a.local"
                );

        Store storeB =
                createStore(
                        "Variant Store B",
                        "inventory-variant-b.local"
                );

        Producto productoA =
                createProduct(
                        storeA,
                        "Producto Variante A",
                        0
                );

        ProductoVariante varianteA =
                createVariant(
                        productoA,
                        5
                );

        flushAndClear();

        Optional<ProductoVariante> result =
                varianteRepository.findByIdForUpdate(
                        varianteA.getId(),
                        storeB
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldFindVariantOnlyForItsOwnStore() {

        Store store =
                createStore(
                        "Variant Owner Store",
                        "inventory-variant-owner.local"
                );

        Producto producto =
                createProduct(
                        store,
                        "Producto Variante",
                        0
                );

        ProductoVariante variante =
                createVariant(
                        producto,
                        8
                );

        flushAndClear();

        Optional<ProductoVariante> result =
                varianteRepository.findByIdAndStore(
                        variante.getId(),
                        store
                );

        assertThat(result)
                .isPresent();

        assertThat(result.get()
                .getProducto()
                .getStore()
                .getId())
                .isEqualTo(store.getId());
    }

    /*
     * =========================================================
     * ALERT MULTI-TENANCY
     * =========================================================
     */

    @Test
    void shouldCountOnlyAlertsBelongingToRequestedStore() {

        Store storeA =
                createStore(
                        "Alert Store A",
                        "inventory-alert-a.local"
                );

        Store storeB =
                createStore(
                        "Alert Store B",
                        "inventory-alert-b.local"
                );

        Producto productoA =
                createProduct(
                        storeA,
                        "Producto Alert A",
                        2
                );

        Producto productoB =
                createProduct(
                        storeB,
                        "Producto Alert B",
                        1
                );

        createAlert(
                storeA,
                productoA,
                InventoryAlertLevel.CRITICAL,
                2,
                "TEST:STORE:A"
        );

        createAlert(
                storeB,
                productoB,
                InventoryAlertLevel.CRITICAL,
                1,
                "TEST:STORE:B"
        );

        flushAndClear();

        long countA =
                alertRepository.countByStoreAndStatusIn(
                        storeA,
                        List.of(
                                InventoryAlertStatus.OPEN,
                                InventoryAlertStatus.ACKNOWLEDGED
                        )
                );

        long countB =
                alertRepository.countByStoreAndStatusIn(
                        storeB,
                        List.of(
                                InventoryAlertStatus.OPEN,
                                InventoryAlertStatus.ACKNOWLEDGED
                        )
                );

        assertThat(countA)
                .isEqualTo(1);

        assertThat(countB)
                .isEqualTo(1);
    }

    @Test
    void shouldNotLoadAlertUsingAnotherStore() {

        Store storeA =
                createStore(
                        "Alert Owner A",
                        "inventory-alert-owner-a.local"
                );

        Store storeB =
                createStore(
                        "Alert Owner B",
                        "inventory-alert-owner-b.local"
                );

        Producto producto =
                createProduct(
                        storeA,
                        "Producto alerta protegida",
                        2
                );

        InventoryAlert alert =
                createAlert(
                        storeA,
                        producto,
                        InventoryAlertLevel.CRITICAL,
                        2,
                        "TEST:PROTECTED:ALERT"
                );

        flushAndClear();

        Optional<InventoryAlert> result =
                alertRepository.findByIdAndStore(
                        alert.getId(),
                        storeB
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldRejectAcknowledgementOfAlertFromAnotherStore() {

        Store storeA =
                createStore(
                        "Alert Service A",
                        "inventory-alert-service-a.local"
                );

        Store storeB =
                createStore(
                        "Alert Service B",
                        "inventory-alert-service-b.local"
                );

        Producto producto =
                createProduct(
                        storeA,
                        "Producto alerta servicio",
                        2
                );

        InventoryAlert alert =
                createAlert(
                        storeA,
                        producto,
                        InventoryAlertLevel.CRITICAL,
                        2,
                        "TEST:ACK:STORE:A"
                );

        flushAndClear();

        assertThatThrownBy(() ->
                alertService.acknowledge(
                        alert.getId(),
                        storeB,
                        "other-store-admin@test.local"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Alerta de inventario no encontrada"
                );
    }

    /*
     * =========================================================
     * INVENTORY MOVEMENT MULTI-TENANCY
     * =========================================================
     */

    @Test
    void shouldReturnOnlyMovementsFromRequestedStore() {

        Store storeA =
                createStore(
                        "Movement Store A",
                        "inventory-movement-a.local"
                );

        Store storeB =
                createStore(
                        "Movement Store B",
                        "inventory-movement-b.local"
                );

        Producto productoA =
                createProduct(
                        storeA,
                        "Producto Movimiento A",
                        10
                );

        Producto productoB =
                createProduct(
                        storeB,
                        "Producto Movimiento B",
                        20
                );

        InventoryMovement movementA =
                createMovement(
                        storeA,
                        productoA,
                        InventoryMovementType.ADJUSTMENT_IN,
                        5,
                        5,
                        10
                );

        createMovement(
                storeB,
                productoB,
                InventoryMovementType.ADJUSTMENT_OUT,
                3,
                20,
                17
        );

        flushAndClear();

        List<InventoryMovement> result =
                movementRepository
                        .findByStoreOrderByCreatedAtDesc(
                                storeA
                        );

        assertThat(result)
                .extracting(InventoryMovement::getId)
                .contains(movementA.getId());

        assertThat(result)
                .allSatisfy(movement ->
                        assertThat(
                                movement
                                        .getStore()
                                        .getId()
                        )
                                .isEqualTo(
                                        storeA.getId()
                                )
                );
    }

    @Test
    void shouldNotReturnProductMovementsFromAnotherStore() {

        Store storeA =
                createStore(
                        "Movement Product A",
                        "inventory-product-movement-a.local"
                );

        Store storeB =
                createStore(
                        "Movement Product B",
                        "inventory-product-movement-b.local"
                );

        Producto productoA =
                createProduct(
                        storeA,
                        "Movimiento protegido A",
                        10
                );

        createMovement(
                storeA,
                productoA,
                InventoryMovementType.ADJUSTMENT_IN,
                2,
                8,
                10
        );

        flushAndClear();

        List<InventoryMovement> result =
                movementRepository
                        .findByProductoIdAndStoreOrderByCreatedAtDesc(
                                productoA.getId(),
                                storeB
                        );

        assertThat(result)
                .isEmpty();
    }

    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private Store createStore(
            String name,
            String domain
    ) {

        Store store = new Store();

        store.setNombre(name);
        store.setDominio(domain);
        store.setActiva(true);

        return storeRepository.saveAndFlush(store);
    }

    private Categoria createCategory(
            Store store,
            String name
    ) {

        Categoria categoria =
                new Categoria();

        categoria.setNombre(name);
        categoria.setStore(store);

        return categoriaRepository.saveAndFlush(
                categoria
        );
    }

    private Producto createProduct(
            Store store,
            String name,
            int stock
    ) {

        Categoria categoria =
                createCategory(
                        store,
                        "Categoría " + name
                );

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

        return productoRepository.saveAndFlush(
                producto
        );
    }

    private ProductoVariante createVariant(
            Producto producto,
            int stock
    ) {

        ProductoVariante variante =
                new ProductoVariante();

        variante.setProducto(producto);
        variante.setStock(stock);
        variante.setPrincipal(false);

        return varianteRepository.saveAndFlush(
                variante
        );
    }

    private InventoryAlert createAlert(
            Store store,
            Producto producto,
            InventoryAlertLevel level,
            int stock,
            String activeKey
    ) {

        InventoryAlert alert =
                new InventoryAlert();

        alert.setStore(store);
        alert.setProducto(producto);
        alert.setLevel(level);
        alert.setStatus(
                InventoryAlertStatus.OPEN
        );
        alert.setCurrentStock(stock);
        alert.setStockThreshold(5);
        alert.setActiveKey(activeKey);

        return alertRepository.saveAndFlush(
                alert
        );
    }

    private InventoryMovement createMovement(
            Store store,
            Producto producto,
            InventoryMovementType type,
            int quantity,
            int stockBefore,
            int stockAfter
    ) {

        InventoryMovement movement =
                new InventoryMovement();

        movement.setStore(store);
        movement.setProducto(producto);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setReason(
                "Inventory integration test"
        );

        return movementRepository.saveAndFlush(
                movement
        );
    }

    private void flushAndClear() {

        entityManager.flush();
        entityManager.clear();
    }
}