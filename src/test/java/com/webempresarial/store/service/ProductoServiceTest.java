package com.webempresarial.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.dto.producto.shared.ProductoVarianteDTO;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.ImagenProducto;
import com.webempresarial.store.model.Marca;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ImagenProductoRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;
import com.webempresarial.store.theme.StoreResolver;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ImagenProductoRepository imagenProductoRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private MarcaService marcaService;

    @Mock
    private ProductoVarianteRepository productoVarianteRepository;

    @Mock
    private StoreResolver storeResolver;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(
                productoRepository,
                imagenProductoRepository,
                cloudinaryService,
                categoriaService,
                marcaService,
                productoVarianteRepository,
                storeResolver
        );
    }
    @Test
    void eliminarProducto_shouldFailWhenProductDoesNotExistForStore() {

        Store store = mock(Store.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.eliminarProducto(10L, store)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No existe");

        verify(productoRepository, never())
                .delete(any());

        verifyNoInteractions(cloudinaryService);
    }
    @Test
    void eliminarProducto_shouldDeleteCloudinaryImagesAndProduct() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        ImagenProducto imagen1 =
                mock(ImagenProducto.class);

        ImagenProducto imagen2 =
                mock(ImagenProducto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getImagenes())
                .thenReturn(
                        Set.of(
                                imagen1,
                                imagen2
                        )
                );

        when(imagen1.getPublicId())
                .thenReturn("img_1");

        when(imagen2.getPublicId())
                .thenReturn("img_2");

        service.eliminarProducto(10L, store);

        verify(cloudinaryService)
                .eliminarImagen("img_1");

        verify(cloudinaryService)
                .eliminarImagen("img_2");

        verify(productoRepository)
                .delete(producto);
    }
    
    @Test
    void eliminarProducto_shouldDeleteProductEvenWhenCloudinaryDeletionFails()
            throws Exception {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        ImagenProducto imagen =
                mock(ImagenProducto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getImagenes())
                .thenReturn(
                        Set.of(imagen)
                );

        when(imagen.getPublicId())
                .thenReturn("img_problem");

        doThrow(new RuntimeException("Cloudinary unavailable"))
                .when(cloudinaryService)
                .eliminarImagen("img_problem");

        service.eliminarProducto(10L, store);

        verify(cloudinaryService)
                .eliminarImagen("img_problem");

        verify(productoRepository)
                .delete(producto);
    }
    @Test
    void ajustarPrecioCategoria_shouldRejectNullCategory() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.ajustarPrecioCategoria(
                        null,
                        new BigDecimal("10"),
                        "MONTO",
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La categoría es obligatoria");

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(categoriaService);
    }
    @Test
    void ajustarPrecioCategoria_shouldRejectZeroValue() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.ajustarPrecioCategoria(
                        5L,
                        BigDecimal.ZERO,
                        "MONTO",
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El valor debe ser distinto de cero");

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(categoriaService);
    }
    @Test
    void ajustarPrecioCategoria_shouldRejectInvalidMode() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.ajustarPrecioCategoria(
                        5L,
                        new BigDecimal("10"),
                        "INVALID",
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Modo de ajuste inválido");

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(categoriaService);
    }
    @Test
    void ajustarPrecioCategoria_shouldFailWhenCategoryHasNoProducts() {

        Store store = mock(Store.class);
        Categoria categoria = mock(Categoria.class);

        when(categoriaService.obtenerPorId(5L, store))
                .thenReturn(categoria);

        when(productoRepository.findByCategoriaIdAndStore(5L, store))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                service.ajustarPrecioCategoria(
                        5L,
                        new BigDecimal("10"),
                        "MONTO",
                        store
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "No hay productos en la categoría seleccionada"
                );

        verify(productoRepository, never())
                .saveAll(any());
    }
    @Test
    void ajustarPrecioCategoria_shouldAdjustProductAndVariantPricesByPercentage() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);
        ProductoVariante variante = mock(ProductoVariante.class);

        when(store.getId()).thenReturn(1L);

        when(categoriaService.obtenerPorId(5L, store))
                .thenReturn(mock(Categoria.class));

        when(productoRepository.findByCategoriaIdAndStore(5L, store))
                .thenReturn(List.of(producto));

        when(producto.getPrice())
                .thenReturn(new BigDecimal("100.00"));

        when(producto.getVariantes())
                .thenReturn(Set.of(variante));

        when(variante.getPrecio())
                .thenReturn(new BigDecimal("200.00"));

        service.ajustarPrecioCategoria(
                5L,
                new BigDecimal("10"),
                "PORCENTAJE",
                store
        );

        verify(producto)
                .setPrice(new BigDecimal("110.00"));

        verify(variante)
                .setPrecio(new BigDecimal("220.00"));

        verify(productoRepository)
                .saveAll(List.of(producto));
    }
    @Test
    void ajustarPrecioCategoria_shouldNeverReducePriceBelowOneCent() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(store.getId()).thenReturn(1L);

        when(categoriaService.obtenerPorId(5L, store))
                .thenReturn(mock(Categoria.class));

        when(productoRepository.findByCategoriaIdAndStore(5L, store))
                .thenReturn(List.of(producto));

        when(producto.getPrice())
                .thenReturn(new BigDecimal("10.00"));

        when(producto.getVariantes())
                .thenReturn(Set.of());

        service.ajustarPrecioCategoria(
                5L,
                new BigDecimal("-50.00"),
                "MONTO",
                store
        );

        verify(producto)
                .setPrice(new BigDecimal("0.01"));
    }
    @Test
    void crearProducto_shouldCreateSimpleProductWithStoreAndStock() {

        Store store = mock(Store.class);

        ProductoAdminDTO dto = new ProductoAdminDTO();
        dto.setProductName("  Producto Demo  ");
        dto.setPrecio(new BigDecimal("150.00"));
        dto.setDescription("Descripción");
        dto.setStockSimple(8);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto result =
                service.crearProducto(
                        dto,
                        null,
                        store
                );

        assertThat(result.getStore())
                .isSameAs(store);

        assertThat(result.getProductName())
                .isEqualTo("Producto Demo");

        assertThat(result.getPrice())
                .isEqualByComparingTo("150.00");

        assertThat(result.getDescription())
                .isEqualTo("Descripción");

        assertThat(result.getStockSimple())
                .isEqualTo(8);

        assertThat(result.getVariantes())
                .isEmpty();

        assertThat(result.getTienePromocion())
                .isFalse();

        assertThat(result.getPorcentajeDescuento())
                .isEqualTo(0.0);
    }
    @Test
    void crearProducto_shouldResolveExistingCategoryAndBrandWithinStore() {

        Store store = mock(Store.class);
        Categoria categoria = mock(Categoria.class);
        Marca marca = mock(Marca.class);

        ProductoAdminDTO dto = new ProductoAdminDTO();
        dto.setProductName("Producto");
        dto.setPrecio(new BigDecimal("100.00"));
        dto.setCategoriaId(10L);
        dto.setMarcaId(20L);

        when(categoriaService.obtenerPorId(10L, store))
                .thenReturn(categoria);

        when(marcaService.obtenerPorId(20L, store))
                .thenReturn(marca);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto result =
                service.crearProducto(
                        dto,
                        null,
                        store
                );

        assertThat(result.getCategoria())
                .isSameAs(categoria);

        assertThat(result.getMarca())
                .isSameAs(marca);

        verify(categoriaService)
                .obtenerPorId(10L, store);

        verify(marcaService)
                .obtenerPorId(20L, store);
    }
    @Test
    void crearProducto_shouldCreateCategoryAndBrandByTrimmedName() {

        Store store = mock(Store.class);
        Categoria categoria = mock(Categoria.class);
        Marca marca = mock(Marca.class);

        ProductoAdminDTO dto = new ProductoAdminDTO();
        dto.setProductName("Producto");
        dto.setPrecio(new BigDecimal("100.00"));
        dto.setNuevaCategoria("  Bebidas  ");
        dto.setMarcaNombre("  Demo Brand  ");

        when(
                categoriaService.obtenerOCrearCategoria(
                        "Bebidas",
                        store
                )
        ).thenReturn(categoria);

        when(
                marcaService.obtenerOCrear(
                        "Demo Brand",
                        store
                )
        ).thenReturn(marca);

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto result =
                service.crearProducto(
                        dto,
                        null,
                        store
                );

        assertThat(result.getCategoria())
                .isSameAs(categoria);

        assertThat(result.getMarca())
                .isSameAs(marca);
    }
    @Test
    void crearProducto_shouldCreateVariantsAndForceSimpleStockToZero() {

        Store store = mock(Store.class);

        ProductoVarianteDTO varianteDto =
                new ProductoVarianteDTO();

        varianteDto.setStock(4);
        varianteDto.setPrecio(
                new BigDecimal("125.50")
        );
        varianteDto.setAtributos(
                Map.of(
                        " color ",
                        " Verde ",
                        " talla ",
                        " M "
                )
        );

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        dto.setProductName("Producto");
        dto.setPrecio(new BigDecimal("100.00"));
        dto.setStockSimple(99);
        dto.setVariantes(
                List.of(varianteDto)
        );

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto result =
                service.crearProducto(
                        dto,
                        null,
                        store
                );

        assertThat(result.getStockSimple())
                .isZero();

        assertThat(result.getVariantes())
                .hasSize(1);

        ProductoVariante variante =
                result.getVariantes()
                        .iterator()
                        .next();

        assertThat(variante.getProducto())
                .isSameAs(result);

        assertThat(variante.getStock())
                .isEqualTo(4);

        assertThat(variante.getPrecio())
                .isEqualByComparingTo("125.50");

        assertThat(variante.getAtributosMap())
                .containsEntry("color", "Verde")
                .containsEntry("talla", "M");
    }
    @Test
    void obtenerProducto_shouldReturnProductForStore() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        Producto result =
                service.obtenerProducto(10L, store);

        assertThat(result).isSameAs(producto);

        verify(productoRepository)
                .findByIdConTodo(10L, store);
    }
    @Test
    void obtenerProducto_shouldFailWhenProductDoesNotExistForStore() {

        Store store = mock(Store.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.obtenerProducto(10L, store)
        )
                .isInstanceOf(
                        ProductoService.ProductoNotFoundException.class
                );
    }
    @Test
    void toggleVisibility_shouldDisableVisibleProduct() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.isVisibleEnMenu())
                .thenReturn(true, false);

        boolean result =
                service.toggleVisibility(10L, store);

        assertThat(result).isFalse();

        verify(producto)
                .setVisibleEnMenu(false);

        verify(productoRepository)
                .save(producto);
    }
    @Test
    void toggleVisibility_shouldEnableHiddenProduct() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.isVisibleEnMenu())
                .thenReturn(false, true);

        boolean result =
                service.toggleVisibility(10L, store);

        assertThat(result).isTrue();

        verify(producto)
                .setVisibleEnMenu(true);

        verify(productoRepository)
                .save(producto);
    }
    @Test
    void togglePromocion_shouldEnablePromotionWhenDisabled() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getTienePromocion())
                .thenReturn(false, true);

        boolean result =
                service.togglePromocion(10L, store);

        assertThat(result).isTrue();

        verify(producto)
                .setTienePromocion(true);

        verify(productoRepository)
                .save(producto);
    }
    @Test
    void togglePromocion_shouldEnablePromotionWhenCurrentValueIsNull() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getTienePromocion())
                .thenReturn(null, true);

        boolean result =
                service.togglePromocion(10L, store);

        assertThat(result).isTrue();

        verify(producto)
                .setTienePromocion(true);
    }
    @Test
    void togglePromocion_shouldDisablePromotionWhenEnabled() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getTienePromocion())
                .thenReturn(true, false);

        boolean result =
                service.togglePromocion(10L, store);

        assertThat(result).isFalse();

        verify(producto)
                .setTienePromocion(false);

        verify(productoRepository)
                .save(producto);
    }
    @Test
    void actualizarPrecio_shouldRejectNullPrice() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.actualizarPrecio(
                        10L,
                        null,
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precio inválido");

        verifyNoInteractions(productoRepository);
    }
    @Test
    void actualizarPrecio_shouldRejectNegativePrice() {

        Store store = mock(Store.class);

        assertThatThrownBy(() ->
                service.actualizarPrecio(
                        10L,
                        new BigDecimal("-0.01"),
                        store
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precio inválido");

        verifyNoInteractions(productoRepository);
    }
    @Test
    void actualizarPrecio_shouldRejectProductWithVariants() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getVariantes())
                .thenReturn(
                        Set.of(
                                mock(ProductoVariante.class)
                        )
                );

        assertThatThrownBy(() ->
                service.actualizarPrecio(
                        10L,
                        new BigDecimal("100.00"),
                        store
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Producto con variantes no usa precio base"
                );

        verify(producto, never())
                .setPrice(any());
    }
    @Test
    void actualizarPrecio_shouldRoundPriceHalfUpToTwoDecimals() {

        Store store = mock(Store.class);
        Producto producto = mock(Producto.class);

        when(productoRepository.findByIdConTodo(10L, store))
                .thenReturn(Optional.of(producto));

        when(producto.getVariantes())
                .thenReturn(Collections.emptySet());

        service.actualizarPrecio(
                10L,
                new BigDecimal("12.345"),
                store
        );

        verify(producto)
                .setPrice(
                        new BigDecimal("12.35")
                );
    }
    @Test
    void actualizarStockVariante_shouldUpdateVariantBelongingToStore() {

        Store store = mock(Store.class);
        ProductoVariante variante =
                mock(ProductoVariante.class);

        when(
                productoVarianteRepository
                        .findByIdAndStore(20L, store)
        )
                .thenReturn(Optional.of(variante));

        service.actualizarStockVariante(
                20L,
                15,
                store
        );

        verify(variante)
                .setStock(15);
    }
    @Test
    void actualizarStockVariante_shouldFailWhenVariantDoesNotBelongToStore() {

        Store store = mock(Store.class);

        when(
                productoVarianteRepository
                        .findByIdAndStore(20L, store)
        )
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.actualizarStockVariante(
                        20L,
                        15,
                        store
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Variante no existe");
    }
    
}