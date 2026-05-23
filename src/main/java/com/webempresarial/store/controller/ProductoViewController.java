package com.webempresarial.store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.dto.producto.shared.ProductoVarianteDTO;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.CategoriaService;
import com.webempresarial.store.service.MarcaService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
public class ProductoViewController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final StoreResolver storeResolver;

    public ProductoViewController(
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

    @GetMapping("/admin/productos")
    public String verProductos(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute(
                "categorias",
                productoService.obtenerProductosAdminAgrupados(store)
        );

        return "admin/VerProductos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevoProducto(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute("producto", new ProductoAdminDTO());
        model.addAttribute("categorias", categoriaService.obtenerTodas(store));
        model.addAttribute("marcas", marcaService.obtenerTodas(store));

        return "admin/subirProducto";
    }

    @PostMapping("/nuevo")
    public String guardarProducto(
            @Valid @ModelAttribute("producto") ProductoAdminDTO dto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> imagenes,
            @RequestParam(value = "variantesJson", required = false)
            String variantesJson,
            @RequestParam(value = "nuevaCategoria", required = false)
            String nuevaCategoria,
            @RequestParam(value = "nuevaMarca", required = false)
            String nuevaMarca,
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.obtenerTodas(store));
            model.addAttribute("marcas", marcaService.obtenerTodas(store));
            return "admin/subirProducto";
        }

        try {

            if (nuevaCategoria != null && !nuevaCategoria.isBlank()) {
                dto.setCategoriaId(
                        categoriaService
                                .obtenerOCrearCategoria(nuevaCategoria.trim(), store)
                                .getId()
                );
            } else if (dto.getCategoriaId() != null) {
                dto.setCategoriaId(
                        categoriaService
                                .obtenerPorId(dto.getCategoriaId(),store)
                                .getId()
                );
            }

            if (nuevaMarca != null && !nuevaMarca.isBlank()) {
                dto.setMarcaId(
                        marcaService
                                .obtenerOCrear(nuevaMarca.trim(),store)
                                .getId()
                );
            }

            if (variantesJson != null && !variantesJson.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();

                List<ProductoVarianteDTO> variantes =
                        mapper.readValue(
                                variantesJson,
                                new TypeReference<List<ProductoVarianteDTO>>() {}
                        );

                dto.setVariantes(variantes);
            }

            productoService.crearProducto(
                    dto,
                    imagenes,
                    store
            );

            return "redirect:/admin/productos";

        } catch (Exception e) {

            result.reject(
                    "error.producto",
                    "Error al guardar producto: " + e.getMessage()
            );

            model.addAttribute("categorias", categoriaService.obtenerTodas(store));
            model.addAttribute("marcas", marcaService.obtenerTodas(store));
            model.addAttribute("producto", dto);

            return "admin/subirProducto";
        }
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        ProductoAdminDTO dto =
                productoService.obtenerProductoAdmin(id, store);

        model.addAttribute("productoDTO", dto);
        model.addAttribute("categorias", categoriaService.obtenerTodas(store));
        model.addAttribute("marcas", marcaService.obtenerTodas(store));

        return "admin/EditarProducto";
    }

    @PostMapping("/editar/{id}")
    public String editarProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute("productoDTO") ProductoAdminDTO dto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false)
            List<MultipartFile> nuevasImagenes,
            Model model,
            HttpServletRequest request
    ) {

        Store store = storeResolver.getCurrentStore(request);

        if (result.hasErrors()) {
            model.addAttribute("productoDTO", dto);
            model.addAttribute("categorias", categoriaService.obtenerTodas(store));
            model.addAttribute("marcas", marcaService.obtenerTodas(store));
            return "admin/EditarProducto";
        }

        try {

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

            } else {
                datos.setCategoria(
                        categoriaService.obtenerPorId(
                                dto.getCategoriaId(),
                                store
                        )
                );
            }

            if (dto.getMarcaNombre() != null &&
                    !dto.getMarcaNombre().isBlank()) {

                datos.setMarca(
                        marcaService.obtenerOCrear(
                                dto.getMarcaNombre().trim(),store
                                
                        )
                );

            } else if (dto.getMarcaId() != null) {

                datos.setMarca(
                        marcaService.obtenerPorId(
                                dto.getMarcaId(), 
                                store
                        )
                );
            }

            productoService.actualizarProductoCompleto(
                    id,
                    datos,
                    nuevasImagenes,
                    dto.getImagenesEliminar(),
                    dto.getVariantes(),
                    store
            );

            return "redirect:/admin/productos";

        } catch (Exception e) {

            model.addAttribute("productoDTO", dto);
            model.addAttribute("categorias", categoriaService.obtenerTodas(store));
            model.addAttribute("marcas", marcaService.obtenerTodas(store));

            return "admin/EditarProducto";
        }
    }

    @PostMapping("/variantes/{id}/stock")
    public String actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer stock,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        productoService.actualizarStockVariante(
                id,
                stock,
                store
        );

        return "redirect:/admin/productos";
    }

    @GetMapping("/modificar-precios")
    public String vistaModificarPrecios(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        model.addAttribute(
                "productos",
                productoService.obtenerProductosAdminOptimizado(store)
        );

        model.addAttribute(
                "categorias",
                productoService.obtenerCategorias(store)
        );

        return "admin/modificar-precios";
    }
}