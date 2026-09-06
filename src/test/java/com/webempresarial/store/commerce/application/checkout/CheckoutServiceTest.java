package com.webempresarial.store.commerce.application.checkout;


import static org.assertj.core.api.Assertions.assertThat;  
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.commerce.application.catalog.CatalogProductQueryService;
import com.webempresarial.store.commerce.application.order.OrderAuditService;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.commerce.domain.order.Order;
import com.webempresarial.store.commerce.domain.order.Order.PaymentMethod;
import com.webempresarial.store.commerce.domain.order.OrderAuditAction;
import com.webempresarial.store.dto.CustomerDTO;
import com.webempresarial.store.dto.checkout.CartItemDTO;
import com.webempresarial.store.dto.checkout.CheckoutRequestDTO;
import com.webempresarial.store.exceptions.InsufficientStockException;
import com.webempresarial.store.model.Cliente;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.UserService;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.webempresarial.store.model.ProductoVariante;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private OrderAuditService orderAuditService;
    
    @Mock
    private CatalogProductQueryService catalogProductQueryService;

    private CheckoutService service;

    private Store store;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
    	service = new CheckoutService(
    	        orderService,
    	        userService,
    	        orderAuditService,
    	        catalogProductQueryService
    	);

        store = new Store();
        store.setId(1L);
        store.setActiva(true);

        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setStore(store);
        cliente.setEmail("cliente@test.com");
        cliente.setFullName("Cliente Test");
        cliente.setPhone("2221234567");
    }
    
    @Test
    void createOrder_shouldApplyFreeShippingAtThreshold() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto Premium",
                        10,
                        "1250.00"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        when(orderService.crearOrden(
                any(Order.class),
                eq(store)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Order result =
                service.createOrder(request, store);

        assertThat(result.getTotal())
                .isEqualByComparingTo("1250.00");
    }
    
    @Test
    void createOrder_shouldUseVariantPriceInsteadOfProductPrice() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, 10L, 2);

        Producto producto =
                simpleProduct(100L, "Playera", 0, "500.00");

        ProductoVariante variante =
                variant(producto, 10L, 8, "650.00");

        mockCustomerResolution(request);


        when(catalogProductQueryService.obtenerVarianteConLock(10L, store))
                .thenReturn(variante);

        when(orderService.crearOrden(any(Order.class), eq(store)))
                .thenAnswer(i -> i.getArgument(0));

        Order result = service.createOrder(request, store);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getPrice())
                .isEqualByComparingTo("650.00");

        assertThat(result.getTotal())
        .isEqualByComparingTo("1300.00"); // 650 x 2, envío gratis
    }
    @Test
    void createOrder_shouldRejectVariantBelongingToDifferentProduct() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, 10L, 1);

        Producto otro =
                simpleProduct(200L, "Producto B", 10, "100.00");

        ProductoVariante variante =
                variant(otro, 10L, 5, "150.00");

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerVarianteConLock(10L, store))
                .thenReturn(variante);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece");
    }
    
    
    @Test
    void createOrder_shouldRejectInsufficientVariantStock() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, 10L, 4);

        Producto producto =
                simpleProduct(100L, "Producto", 0, "300.00");

        ProductoVariante variante =
                variant(producto, 10L, 2, "320.00");

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerVarianteConLock(10L, store))
                .thenReturn(variante);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(InsufficientStockException.class);
    }
    
    @Test
    void createOrder_shouldRejectInvalidPaymentMethod() {

        CheckoutRequestDTO request =
                validRequest("PAYPAL", 100L, null, 1);

        mockCustomerResolution(request);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Método de pago no válido");
    }
    
    @Test
    void createOrder_shouldNormalizeEmailStoredInOrder() {

        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        request.getCustomer()
                .setEmail("  Cliente@Test.COM ");

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto",
                        10,
                        "100.00"
                );

        when(userService.findOrCreateUserByEmail(
                "  Cliente@Test.COM ",
                "Cliente Test",
                "2221234567",
                store
        )).thenReturn(cliente);

        when(userService.saveUser(cliente, store))
                .thenReturn(cliente);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        when(orderService.crearOrden(
                any(Order.class),
                eq(store)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Order result =
                service.createOrder(
                        request,
                        store
                );

        assertThat(result.getCustomerEmail())
                .isEqualTo("cliente@test.com");
    }
    
    @Test
    void createOrder_shouldRejectNegativePrice() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        Producto producto =
                simpleProduct(100L, "Producto", 10, "-20.00");

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(100L, store))
                .thenReturn(producto);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Precio inválido");
    }
    
    @Test
    void createOrder_shouldRejectNullPrice() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        Producto producto = new Producto();
        producto.setId(100L);
        producto.setProductName("Producto");
        producto.setStockSimple(5);

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(100L, store))
                .thenReturn(producto);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    void createOrder_shouldChargeShippingBelowThreshold() {

        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto",
                        10,
                        "1249.99"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(100L, store))
                .thenReturn(producto);

        when(orderService.crearOrden(any(Order.class), eq(store)))
                .thenAnswer(i -> i.getArgument(0));

        Order result =
                service.createOrder(request, store);

        assertThat(result.getTotal())
                .isEqualByComparingTo("1369.99");
    }
    
    @Test
    void createOrder_shouldRegisterAuditOnlyOnce() {

        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        Producto producto =
                simpleProduct(100L, "Producto", 10, "200.00");

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(100L, store))
                .thenReturn(producto);

        when(orderService.crearOrden(any(Order.class), eq(store)))
                .thenAnswer(i -> i.getArgument(0));

        Order result =
                service.createOrder(request, store);

        verify(orderAuditService)
                .record(
                        result,
                        OrderAuditAction.ORDER_CREATED,
                        null,
                        null,
                        "Orden creada desde checkout"
                );

        verifyNoMoreInteractions(orderAuditService);
    }
    private ProductoVariante variant(
            Producto producto,
            Long id,
            int stock,
            String price
    ) {
        ProductoVariante variante =
                new ProductoVariante();

        variante.setId(id);
        variante.setProducto(producto);
        variante.setStock(stock);
        variante.setPrecio(new BigDecimal(price));

        return variante;
    }
    

    @Test
    void createOrder_shouldCreateStripeOrderUsingServerPrice() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 2);

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto Test",
                        10,
                        "250.00"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        when(orderService.crearOrden(
                any(Order.class),
                eq(store)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Order result =
                service.createOrder(request, store);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod())
                .isEqualTo(PaymentMethod.STRIPE);

        // 250 x 2 = 500 + 120 envío
        assertThat(result.getTotal())
                .isEqualByComparingTo("620.00");

        assertThat(result.getItems())
                .hasSize(1);

        assertThat(result.getItems().get(0).getPrice())
                .isEqualByComparingTo("250.00");

        verify(orderService)
                .crearOrden(any(Order.class), eq(store));

        verify(orderService, never())
                .saveOrderTransferencia(
                        any(Order.class),
                        any(Store.class)
                );

        verify(orderAuditService)
                .record(
                        result,
                        OrderAuditAction.ORDER_CREATED,
                        null,
                        null,
                        "Orden creada desde checkout"
                );
    }

    @Test
    void createOrder_shouldUseTransferFlow() {
        CheckoutRequestDTO request =
                validRequest("TRANSFER", 100L, null, 1);

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto Test",
                        10,
                        "200.00"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        when(orderService.saveOrderTransferencia(
                any(Order.class),
                eq(store)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Order result =
                service.createOrder(request, store);

        assertThat(result.getPaymentMethod())
                .isEqualTo(PaymentMethod.TRANSFER);

        assertThat(result.getTotal())
                .isEqualByComparingTo("320.00");

        verify(orderService)
                .saveOrderTransferencia(
                        any(Order.class),
                        eq(store)
                );

        verify(orderService, never())
                .crearOrden(
                        any(Order.class),
                        any(Store.class)
                );
    }

    @Test
    void createOrder_shouldRejectUnavailableStore() {
        store.setActiva(false);

        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tienda no está disponible");

        verify(orderService, never())
                .crearOrden(any(), any());
    }

    @Test
    void createOrder_shouldRejectEmptyCart() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        request.setCart(List.of());

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El carrito no puede estar vacío");
    }

    @Test
    void createOrder_shouldRejectInsufficientSimpleProductStock() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 5);

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto Test",
                        2,
                        "100.00"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        assertThatThrownBy(() ->
                service.createOrder(request, store)
        )
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(orderService, never())
                .crearOrden(any(), any());
    }

    @Test
    void createOrder_shouldGroupDuplicateCartItems() {
        CheckoutRequestDTO request =
                validRequest("STRIPE", 100L, null, 1);

        CartItemDTO duplicate =
                cartItem(100L, null, 2);

        request.setCart(
                List.of(
                        cartItem(100L, null, 1),
                        duplicate
                )
        );

        Producto producto =
                simpleProduct(
                        100L,
                        "Producto Test",
                        10,
                        "100.00"
                );

        mockCustomerResolution(request);

        when(catalogProductQueryService.obtenerProductoConLock(
                100L,
                store
        )).thenReturn(producto);

        when(orderService.crearOrden(
                any(Order.class),
                eq(store)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        Order result =
                service.createOrder(request, store);

        assertThat(result.getItems())
                .hasSize(1);

        assertThat(result.getItems().get(0).getQuantity())
                .isEqualTo(3);

        // 3 x 100 + 120 envío
     // 3 x 100 + 120 envío
        assertThat(result.getTotal())
                .isEqualByComparingTo("420.00");

        verify(catalogProductQueryService)
        .obtenerProductoConLock(
                100L,
                store
        );
    }

    private void mockCustomerResolution(
            CheckoutRequestDTO request
    ) {
        when(userService.findOrCreateUserByEmail(
                request.getCustomer().getEmail(),
                request.getCustomer().getFullName(),
                request.getCustomer().getPhone(),
                store
        )).thenReturn(cliente);

        when(userService.saveUser(cliente, store))
                .thenReturn(cliente);
    }

    private CheckoutRequestDTO validRequest(
            String paymentMethod,
            Long productId,
            Long variantId,
            int quantity
    ) {
        CustomerDTO customer =
                new CustomerDTO();

        customer.setFullName("Cliente Test");
        customer.setEmail("Cliente@Test.com");
        customer.setPhone("2221234567");
        customer.setAddress("Calle 123 Puebla");

        CheckoutRequestDTO request =
                new CheckoutRequestDTO();

        request.setCustomer(customer);
        request.setPaymentMethod(paymentMethod);
        request.setCart(
                List.of(
                        cartItem(
                                productId,
                                variantId,
                                quantity
                        )
                )
        );

        return request;
    }

    private CartItemDTO cartItem(
            Long productId,
            Long variantId,
            int quantity
    ) {
        CartItemDTO item =
                new CartItemDTO();

        item.setProductId(productId);
        item.setVarianteId(variantId);
        item.setQuantity(quantity);

        return item;
    }

    private Producto simpleProduct(
            Long id,
            String name,
            int stock,
            String price
    ) {
        Producto producto =
                new Producto();

        producto.setId(id);
        producto.setProductName(name);
        producto.setStockSimple(stock);

        /*
         * Ajusta únicamente esta línea si tu Producto
         * no tiene este setter exacto.
         */
        producto.setPrice(
                new BigDecimal(price)
        );

        return producto;
    }
}