package com.webempresarial.store.controller;

import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.dto.producto.publico.ProductoDetailDTO;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CategoriaService;
import com.webempresarial.store.service.MarcaService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final StoreResolver storeResolver;

    public ProductoController(
            ProductoService productoService,
            CategoriaService categoriaService,
            MarcaService marcaService,
            StoreResolver storeResolver
    ) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
        this.storeResolver = storeResolver;
    }

    @GetMapping
    public List<ProductoCardDTO> listarProductos(
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);
        return productoService.obtenerProductosCompletos(store);
    }

    @GetMapping("/{id}")
    public ProductoDetailDTO obtenerProducto(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);
        return productoService.obtenerDetalleProducto(id, store);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);
        productoService.eliminarProducto(id, store);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/editar/{id}")
    public Map<String, Object> actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute ProductoAdminDTO dto,
            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> nuevasImagenes,
            @RequestParam(value = "eliminarImagenes", required = false)
            List<Long> eliminarImagenes,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        Producto datos = new Producto();

        datos.setProductName(dto.getProductName());
        datos.setPrice(dto.getPrecio());
        datos.setDescription(dto.getDescription());
        datos.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        datos.setStockSimple(dto.getStockSimple());

        if (dto.getNuevaCategoria() != null &&
                !dto.getNuevaCategoria().isBlank()) {

            datos.setCategoria(
                    categoriaService.obtenerOCrearCategoria(
                            dto.getNuevaCategoria().trim(),
                            store
                    )
            );

        } else if (dto.getCategoriaId() != null) {

            datos.setCategoria(
                    categoriaService.obtenerPorId(
                            dto.getCategoriaId(),
                            store
                    )
            );

        } else {
            throw new IllegalArgumentException(
                    "Debe seleccionar categoría"
            );
        }

        if (dto.getMarcaId() != null) {
            datos.setMarca(
            		marcaService.obtenerPorId(dto.getMarcaId(), store)            );
        } else {
            datos.setMarca(null);
        }

        Producto actualizado =
                productoService.actualizarProductoCompleto(
                        id,
                        datos,
                        nuevasImagenes,
                        eliminarImagenes,
                        dto.getVariantes(),
                        store
                );

        return Map.of(
                "success", true,
                "productoId", actualizado.getId()
        );
    }

    @DeleteMapping("/{productoId}/eliminar-imagen/{idImagen}")
    public ResponseEntity<Void> eliminarImagenInmediato(
            @PathVariable Long productoId,
            @PathVariable Long idImagen,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        productoService.eliminarImagenInmediatoSeguro(
                productoId,
                idImagen,
                store
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/togglePromocion")
    public Boolean togglePromocion(
            @RequestParam Long productoId,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);
        return productoService.togglePromocion(productoId, store);
    }

    @PostMapping("/toggleVisibility")
    public Boolean toggleVisibility(
            @RequestParam Long productoId,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);
        return productoService.toggleVisibility(productoId, store);
    }

    @PostMapping("/toggleCategoria")
    public ResponseEntity<Void> toggleCategoria(
            @RequestParam Long categoriaId,
            @RequestParam boolean visible,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        productoService.toggleVisibilidadPorCategoria(
                categoriaId,
                visible,
                store
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/actualizarPrecio")
    public ResponseEntity<?> actualizarPrecio(
            @RequestParam Long productoId,
            @RequestParam BigDecimal precio,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        productoService.actualizarPrecio(
                productoId,
                precio,
                store
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/ajustarPrecioCategoria")
    public ResponseEntity<Void> ajustarPrecioCategoria(
            @RequestParam Long categoriaId,
            @RequestParam BigDecimal valor,
            @RequestParam String modo,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        productoService.ajustarPrecioCategoria(
                categoriaId,
                valor,
                modo,
                store
        );

        return ResponseEntity.ok().build();
    }
}