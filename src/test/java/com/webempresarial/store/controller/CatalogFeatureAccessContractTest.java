package com.webempresarial.store.controller;

import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CategoriaService;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.MarcaService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.service.ProductoVarianteService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogFeatureAccessContractTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private MarcaService marcaService;

    @Mock
    private ProductoVarianteService varianteService;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    private Store store;

    private ProductoViewController viewController;
    private ProductoController productoController;
    private VarianteController varianteController;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeResolver.getCurrentStore(request)
        )
        .thenReturn(store);

        viewController =
                new ProductoViewController(
                        productoService,
                        categoriaService,
                        marcaService,
                        storeResolver,
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        productoController =
                new ProductoController(
                        productoService,
                        categoriaService,
                        marcaService,
                        storeResolver,
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        varianteController =
                new VarianteController(
                        varianteService,
                        storeResolver,
                        featureAccessService
                );
    }

    @Test
    void productViewsShouldRequireProductsFeature() {

        lock(Feature.PRODUCTS);

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        assertThatThrownBy(
                () ->
                        viewController.verProductos(
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        viewController.formularioNuevoProducto(
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        viewController.guardarProducto(
                                dto,
                                bindingResult,
                                null,
                                null,
                                null,
                                null,
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        viewController.formularioEditar(
                                1L,
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        viewController.editarProducto(
                                1L,
                                dto,
                                bindingResult,
                                null,
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        viewController.vistaModificarPrecios(
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        verify(
                featureAccessService,
                times(6)
        )
        .requireFeature(
                store,
                Feature.PRODUCTS
        );

        verifyNoInteractions(
                productoService,
                categoriaService,
                marcaService,
                currentAdminAuthorizationService
        );
    }

    @Test
    void variantStockShouldRequireInventoryFeature() {

        lock(Feature.INVENTORY);

        assertThatThrownBy(
                () ->
                        viewController.actualizarStock(
                                1L,
                                5,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.INVENTORY
                );

        verify(
                featureAccessService,
                never()
        )
        .requireFeature(
                store,
                Feature.PRODUCTS
        );

        verifyNoInteractions(productoService);
    }

    @Test
    void allProductRestOperationsShouldRequireProductsFeature() {

        lock(Feature.PRODUCTS);

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        assertThatThrownBy(
                () ->
                        productoController.listarProductos(
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.obtenerProducto(
                                1L,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.eliminarProducto(
                                1L,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.actualizarProducto(
                                1L,
                                dto,
                                null,
                                null,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.eliminarImagenInmediato(
                                1L,
                                2L,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.togglePromocion(
                                1L,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.toggleVisibility(
                                1L,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.toggleCategoria(
                                1L,
                                true,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.actualizarPrecio(
                                1L,
                                BigDecimal.TEN,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        assertThatThrownBy(
                () ->
                        productoController.ajustarPrecioCategoria(
                                1L,
                                BigDecimal.TEN,
                                "PERCENT",
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        verify(
                featureAccessService,
                times(10)
        )
        .requireFeature(
                store,
                Feature.PRODUCTS
        );

        verifyNoInteractions(
                productoService,
                categoriaService,
                marcaService,
                currentAdminAuthorizationService
        );
    }

    @Test
    void variantPriceShouldRequireProductsFeature() {

        lock(Feature.PRODUCTS);

        assertThatThrownBy(
                () ->
                        varianteController.actualizarPrecio(
                                1L,
                                BigDecimal.TEN,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verifyNoInteractions(varianteService);
    }

    @Test
    void newCategoryShouldRequireCategoryManageBeforeCreation() {

        denyPermission(
                AdminPermission.CATEGORY_MANAGE
        );

        assertThatThrownBy(
                () ->
                        viewController.guardarProducto(
                                new ProductoAdminDTO(),
                                bindingResult,
                                null,
                                null,
                                "Nueva categoría",
                                null,
                                model,
                                request
                        )
        )
        .isInstanceOf(AccessDeniedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.CATEGORY_MANAGE
                );

        verify(
                featureAccessService,
                never()
        )
        .requireFeature(
                store,
                Feature.CATEGORIES
        );

        verifyNoInteractions(
                categoriaService,
                marcaService,
                productoService
        );
    }

    @Test
    void newCategoryShouldRequireCategoriesFeatureAfterPermission() {

        lock(Feature.CATEGORIES);

        assertThatThrownBy(
                () ->
                        viewController.guardarProducto(
                                new ProductoAdminDTO(),
                                bindingResult,
                                null,
                                null,
                                "Nueva categoría",
                                null,
                                model,
                                request
                        )
        )
        .isInstanceOf(FeatureLockedException.class);

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.CATEGORY_MANAGE
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CATEGORIES
                );

        verifyNoInteractions(
                categoriaService,
                marcaService,
                productoService
        );
    }

    @Test
    void newBrandShouldRequireBrandManageBeforeCreation() {

        denyPermission(
                AdminPermission.BRAND_MANAGE
        );

        assertThatThrownBy(
                () ->
                        viewController.guardarProducto(
                                new ProductoAdminDTO(),
                                bindingResult,
                                null,
                                null,
                                null,
                                "Nueva marca",
                                model,
                                request
                        )
        )
        .isInstanceOf(AccessDeniedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.BRAND_MANAGE
                );

        verify(
                currentAdminAuthorizationService,
                never()
        )
        .requirePermission(
                AdminPermission.CATEGORY_MANAGE
        );

        verifyNoInteractions(
                categoriaService,
                marcaService,
                productoService
        );
    }

    @Test
    void existingCategoryAndBrandShouldNotRequireManagePermissions() {

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        dto.setCategoriaId(3L);
        dto.setMarcaId(4L);

        Categoria categoria =
                mock(Categoria.class);

        when(categoria.getId())
                .thenReturn(3L);

        when(
                categoriaService.obtenerPorId(
                        3L,
                        store
                )
        )
        .thenReturn(categoria);

        String result =
                viewController.guardarProducto(
                        dto,
                        bindingResult,
                        null,
                        null,
                        null,
                        null,
                        model,
                        request
                );

        assertThat(result)
                .isEqualTo(
                        "redirect:/admin/productos"
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(
                featureAccessService,
                never()
        )
        .requireFeature(
                store,
                Feature.CATEGORIES
        );

        verify(
                currentAdminAuthorizationService,
                never()
        )
        .requirePermission(
                AdminPermission.CATEGORY_MANAGE
        );

        verify(
                currentAdminAuthorizationService,
                never()
        )
        .requirePermission(
                AdminPermission.BRAND_MANAGE
        );

        verify(productoService)
                .crearProducto(
                        dto,
                        null,
                        store
                );
    }

    @Test
    void editWithNewCategoryAndBrandShouldRequireBothManagePermissions() {

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        dto.setNuevaCategoria(
                "Nueva categoría"
        );

        dto.setMarcaNombre(
                "Nueva marca"
        );

        denyPermission(
                AdminPermission.BRAND_MANAGE
        );

        assertThatThrownBy(
                () ->
                        viewController.editarProducto(
                                1L,
                                dto,
                                bindingResult,
                                null,
                                model,
                                request
                        )
        )
        .isInstanceOf(AccessDeniedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.CATEGORY_MANAGE
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CATEGORIES
                );

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.BRAND_MANAGE
                );

        verifyNoInteractions(
                categoriaService,
                marcaService,
                productoService
        );
    }

    @Test
    void restEditWithNewCategoryShouldRequireCategoryManage() {

        ProductoAdminDTO dto =
                new ProductoAdminDTO();

        dto.setNuevaCategoria(
                "Nueva categoría"
        );

        denyPermission(
                AdminPermission.CATEGORY_MANAGE
        );

        assertThatThrownBy(
                () ->
                        productoController.actualizarProducto(
                                1L,
                                dto,
                                null,
                                null,
                                request
                        )
        )
        .isInstanceOf(AccessDeniedException.class);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PRODUCTS
                );

        verify(currentAdminAuthorizationService)
                .requirePermission(
                        AdminPermission.CATEGORY_MANAGE
                );

        verify(
                featureAccessService,
                never()
        )
        .requireFeature(
                store,
                Feature.CATEGORIES
        );

        verifyNoInteractions(
                categoriaService,
                marcaService,
                productoService
        );
    }

    private void lock(
            Feature feature
    ) {
        doAnswer(invocation -> {

            Feature requested =
                    invocation.getArgument(1);

            if (requested == feature) {
                throw new FeatureLockedException(
                        feature
                );
            }

            return null;
        })
        .when(featureAccessService)
        .requireFeature(
                eq(store),
                any(Feature.class)
        );
    }

    private void denyPermission(
            AdminPermission permission
    ) {
        doAnswer(invocation -> {

            AdminPermission requested =
                    invocation.getArgument(0);

            if (requested == permission) {
                throw new AccessDeniedException(
                        permission.name()
                                + " required"
                );
            }

            return null;
        })
        .when(currentAdminAuthorizationService)
        .requirePermission(
                any(AdminPermission.class)
        );
    }
}
