package com.webempresarial.store.service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.dto.producto.admin.CategoriaGrupoAdminDTO;
import com.webempresarial.store.dto.producto.admin.ProductoAdminDTO;
import com.webempresarial.store.dto.producto.admin.ProductoAdminListDTO;
import com.webempresarial.store.dto.producto.publico.CategoriaGrupoCardDTO;
import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.dto.producto.publico.ProductoDetailDTO;
import com.webempresarial.store.dto.producto.shared.ProductoVarianteDTO;
import com.webempresarial.store.exception.ImageUploadException;
import com.webempresarial.store.mapper.ProductoAdminListMapper;
import com.webempresarial.store.mapper.ProductoMapper;
import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.ImagenProducto;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.VarianteAtributo;
import com.webempresarial.store.repository.ImagenProductoRepository;
import com.webempresarial.store.repository.ProductoRepository;
import com.webempresarial.store.repository.ProductoVarianteRepository;
import com.webempresarial.store.theme.StoreResolver;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoService {

    private static final Logger log =
            LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final ImagenProductoRepository imagenProductoRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final ProductoVarianteRepository productoVarianteRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            ImagenProductoRepository imagenProductoRepository,
            CloudinaryService cloudinaryService,
            CategoriaService categoriaService,
            MarcaService marcaService,
            ProductoVarianteRepository productoVarianteRepository,
            StoreResolver storeResolver
    ) {
        this.productoRepository = productoRepository;
        this.imagenProductoRepository = imagenProductoRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
        this.productoVarianteRepository = productoVarianteRepository;
    }


    
    
    public List<Producto> obtenerProductosVisiblesConTodo(
            Store store
    ) {

        return productoRepository
                .findProductosVisiblesConTodo(store);
    }

    public List<String> obtenerCategorias(Store store) {

        return productoRepository
                .obtenerNombresCategoriasVisibles(store);
    }
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasAdmin(
            Store store
    ) {
        return categoriaService.obtenerTodas(store);
    }   
    

    @Transactional
    public Producto obtenerProducto(
            Long id,
            Store store
    ) {

        return productoRepository
                .findByIdConTodo(id, store)
                .orElseThrow(() ->
                        new ProductoNotFoundException(id)
                );
    }

    public Optional<Producto> buscarPorNombre(
            String nombre,
            Store store
    ) {

        return productoRepository
                .findByProductNameAndStore(nombre, store);
    }

    
    @Transactional(readOnly = true)
    public List<ProductoCardDTO> obtenerProductosCompletos(
            Store store
    ) {

        return productoRepository
                .findProductosVisiblesConTodo(store)
                .stream()
                .map(ProductoMapper::toCard)
                .toList();
    }


    @Transactional
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(
            Long id,
            Store store
    ) {

        Producto producto = productoRepository
                .findByIdConTodo(id, store)
                .orElseThrow(() ->
                        new RuntimeException("No existe")
                );

        producto.getImagenes().forEach(img -> {

            try {

                cloudinaryService.eliminarImagen(
                        img.getPublicId()
                );

            } catch (Exception e) {

                log.warn(
                        "Error eliminando imagen {}",
                        img.getPublicId()
                );
            }
        });

        productoRepository.delete(producto);
    }
    // ============================================================
    // TOGGLES
    // ============================================================

    @Transactional
    public boolean toggleVisibility(
            Long id,
            Store store
    ) {

        Producto producto =
                obtenerProducto(id, store);

        producto.setVisibleEnMenu(
                !producto.isVisibleEnMenu()
        );

        productoRepository.save(producto);

        return producto.isVisibleEnMenu();
    }

    @Transactional
    public boolean togglePromocion(
            Long id,
            Store store
    ) {

        Producto producto =
                obtenerProducto(id, store);

        producto.setTienePromocion(
                !Boolean.TRUE.equals(
                        producto.getTienePromocion()
                )
        );

        productoRepository.save(producto);

        return producto.getTienePromocion();
    }

 // ============================================================
 // ACTUALIZACIÓN COMPLETA PRO (ESTABLE)
 // ============================================================

 @Transactional
 public Producto actualizarProductoCompleto(
         Long productoId,
         Producto datos,
         List<MultipartFile> nuevasImagenes,
         List<Long> eliminarImagenes,
         List<ProductoVarianteDTO> variantesDTO,
         Store store
 ) {

	 Producto producto = obtenerProducto(productoId, store);
     // =====================================================
     // 1. DATOS BASE
     // =====================================================
     producto.setProductName(datos.getProductName());
     producto.setPrice(datos.getPrice());
     producto.setDescription(datos.getDescription());

     producto.setPorcentajeDescuento(
             datos.getPorcentajeDescuento() != null
                     ? datos.getPorcentajeDescuento()
                     : 0.0
     );

     producto.setTienePromocion(
             producto.getPorcentajeDescuento() > 0
     );

     if (datos.getCategoria() != null) {
         producto.setCategoria(datos.getCategoria());
     }

     // permite quitar marca también
     producto.setMarca(datos.getMarca());

     List<String> publicIdsSubidos = new ArrayList<>();

     try {

         // =====================================================
         // 2. ELIMINAR IMÁGENES
         // =====================================================
         if (eliminarImagenes != null && !eliminarImagenes.isEmpty()) {

             producto.getImagenes().removeIf(img -> {

                 boolean eliminar =
                         eliminarImagenes.contains(img.getId());

                 if (eliminar && img.getPublicId() != null) {
                     try {
                         cloudinaryService.eliminarImagen(
                                 img.getPublicId()
                         );
                     } catch (Exception e) {
                         log.warn(
                                 "No se pudo eliminar imagen {}",
                                 img.getPublicId()
                         );
                     }
                 }

                 return eliminar;
             });
         }

         // =====================================================
         // 3. SUBIR NUEVAS IMÁGENES
         // =====================================================
         if (nuevasImagenes != null) {

             for (MultipartFile file : nuevasImagenes) {

                 if (file == null || file.isEmpty()) {
                     continue;
                 }

                 var subida =
                         cloudinaryService.subirImagen(file);

                 ImagenProducto img =
                         new ImagenProducto();

                 img.setProducto(producto);
                 img.setImageUrl(subida.getSecureUrl());
                 img.setPublicId(subida.getPublicId());

                 producto.getImagenes().add(img);

                 publicIdsSubidos.add(
                         subida.getPublicId()
                 );
             }
         }

         // =====================================================
         // 4. VARIANTES
         // SOLO SI EL FORM LAS ENVÍA
         // =====================================================
      // =====================================================
      // 4. VARIANTES Y STOCK
      // =====================================================

      if (variantesDTO != null && !variantesDTO.isEmpty()) {

          // Producto administrado mediante variantes
          actualizarVariantes(
                  producto,
                  variantesDTO
          );

          producto.setStockSimple(0);

          log.info(
                  "Producto con variantes id={}, cantidadVariantes={}",
                  producto.getId(),
                  variantesDTO.size()
          );

      } else {

          // Producto simple sin variantes
          Integer stockAnterior =
                  producto.getStockSimple();

          Integer nuevoStock =
                  datos.getStockSimple() != null
                          ? datos.getStockSimple()
                          : 0;

          if (nuevoStock < 0) {
              throw new IllegalArgumentException(
                      "El stock no puede ser negativo"
              );
          }

          /*
           * Si anteriormente tenía variantes y ahora el formulario
           * ya no envía ninguna, se convierte en producto simple.
           */
          producto.getVariantes().clear();

          producto.setStockSimple(
                  nuevoStock
          );

          log.info(
                  "Producto simple id={}, stockAnterior={}, stockNuevo={}",
                  producto.getId(),
                  stockAnterior,
                  nuevoStock
          );
      }

         // =====================================================
         // 5. VALIDACIÓN
         // =====================================================
         validarProducto(producto);

         log.info(
                 "Producto actualizado id={}",
                 productoId
         );

         return productoRepository.save(producto);

     } catch (Exception e) {

         // rollback cloudinary
         for (String publicId : publicIdsSubidos) {
             try {
                 cloudinaryService.eliminarImagen(publicId);
             } catch (Exception ignored) {
             }
         }

         log.error(
                 "Error actualizando producto {}",
                 productoId,
                 e
         );

         throw new RuntimeException(
                 "Error actualizando producto",
                 e
         );
     }
 }


 // ============================================================
 // MÉTODO PRO ACTUALIZAR VARIANTES
 // ============================================================

 private void actualizarVariantes(
	        Producto producto,
	        List<ProductoVarianteDTO> variantesDTO
	) {

	    // ============================================
	    // VARIANTES ACTUALES EN BD
	    // ============================================
	    Map<Long, ProductoVariante> actuales =
	            producto.getVariantes()
	                    .stream()
	                    .filter(v -> v.getId() != null)
	                    .collect(Collectors.toMap(
	                            ProductoVariante::getId,
	                            v -> v
	                    ));

	    List<ProductoVariante> nuevasLista =
	            new ArrayList<>();

	    // ============================================
	    // RECORRER DTO
	    // ============================================
	    for (ProductoVarianteDTO dto : variantesDTO) {

	        ProductoVariante variante;

	        // ========================================
	        // EXISTENTE
	        // ========================================
	        if (dto.getId() != null &&
	                actuales.containsKey(dto.getId())) {

	            variante = actuales.get(dto.getId());

	        } else {

	            // ====================================
	            // NUEVA
	            // ====================================
	            variante = new ProductoVariante();
	            variante.setProducto(producto);
	        }

	        // ========================================
	        // PRECIO
	        // ========================================
	        BigDecimal nuevoPrecio =
	                dto.getPrecio() != null
	                        ? dto.getPrecio()
	                        : producto.getPrice();

	        if (!Objects.equals(
	                variante.getPrecio(),
	                nuevoPrecio
	        )) {
	            variante.setPrecio(nuevoPrecio);
	        }

	        // ========================================
	        // STOCK
	        // ========================================
	        Integer nuevoStock =
	                dto.getStock() != null
	                        ? dto.getStock()
	                        : 0;

	        if (!Objects.equals(
	                variante.getStock(),
	                nuevoStock
	        )) {
	            variante.setStock(nuevoStock);
	        }

	        // ========================================
	        // ATRIBUTOS SOLO SI CAMBIARON
	        // ========================================
	        Map<String, String> attrsActuales =
	                variante.getAtributos()
	                        .stream()
	                        .collect(Collectors.toMap(
	                                VarianteAtributo::getNombre,
	                                VarianteAtributo::getValor
	                        ));

	        Map<String, String> attrsNuevos =
	                new LinkedHashMap<>();

	        if (dto.getAtributos() != null) {

	            dto.getAtributos().forEach((k, v) -> {

	                if (k != null &&
	                        !k.isBlank() &&
	                        v != null &&
	                        !v.isBlank()) {

	                    attrsNuevos.put(
	                            k.trim(),
	                            v.trim()
	                    );
	                }
	            });
	        }

	        // SOLO SI CAMBIARON
	        if (!attrsActuales.equals(attrsNuevos)) {

	            variante.getAtributos().clear();

	            attrsNuevos.forEach((k, v) -> {

	                VarianteAtributo attr =
	                        new VarianteAtributo();

	                attr.setNombre(k);
	                attr.setValor(v);
	                attr.setVariante(variante);

	                variante.getAtributos().add(attr);
	            });
	        }

	        nuevasLista.add(variante);
	    }

	    // ============================================
	    // SOLO REEMPLAZAR LISTA SI CAMBIÓ
	    // ============================================
	    boolean cambioCantidad =
	            producto.getVariantes().size()
	                    != nuevasLista.size();

	    boolean cambioIds =
	            !producto.getVariantes()
	                    .stream()
	                    .map(ProductoVariante::getId)
	                    .toList()
	                    .equals(
	                            nuevasLista.stream()
	                                    .map(ProductoVariante::getId)
	                                    .toList()
	                    );

	    if (cambioCantidad || cambioIds) {

	        producto.getVariantes().clear();
	        producto.getVariantes().addAll(nuevasLista);
	    }
	}
 
 @Transactional
 public void actualizarStockVariante(
         Long varianteId,
         Integer stock,
         Store store
 ) {

     ProductoVariante variante =
             productoVarianteRepository
                     .findByIdAndStore(
                             varianteId,
                             store
                     )
                     .orElseThrow(() ->
                             new RuntimeException(
                                     "Variante no existe"
                             )
                     );

     variante.setStock(stock);
 }
    // ============================================================
    // MÉTODOS INTERNOS
    // ============================================================

    private void subirImagenesInterno(
            Producto producto,
            List<MultipartFile> nuevasImagenes,
            List<String> publicIdsSubidos
    ) {

        if (nuevasImagenes == null) return;

        List<String> tiposPermitidos = List.of("image/jpeg", "image/png", "image/webp");

        for (MultipartFile archivo : nuevasImagenes) {

            if (archivo.isEmpty()) continue;

            String contentType = archivo.getContentType();
            if (contentType == null || !tiposPermitidos.contains(contentType)) {
                throw new IllegalArgumentException("Formato no permitido");
            }
            log.info("Subiendo imagen. productoId={}", producto.getId());

            CloudinaryUploadResult result;
            try {
                result = cloudinaryService.subirImagen(archivo);
            } catch (IOException e) {
                throw new ImageUploadException(
                        "Error al subir la imagen. Intenta nuevamente.",
                        e
                );
            }

            publicIdsSubidos.add(result.getPublicId());

            ImagenProducto imagen = new ImagenProducto(
                    result.getSecureUrl(),
                    result.getPublicId(),
                    producto
            );

            imagenProductoRepository.save(imagen);
        }
    }

    private void eliminarImagenesInterno(Long productoId, List<Long> ids) {

        if (ids == null || ids.isEmpty()) return;

        List<ImagenProducto> imagenes = imagenProductoRepository.findAllById(ids);

        for (ImagenProducto img : imagenes) {

            if (!img.getProducto().getId().equals(productoId)) {
                throw new IllegalArgumentException("Imagen no pertenece al producto");
            }

            cloudinaryService.eliminarImagen(img.getPublicId());
            imagenProductoRepository.delete(img);
        }
    }
    
    @Transactional(readOnly = true)
    public ProductoDetailDTO obtenerDetalleProducto(
            Long id,
            Store store
    ) {

        Producto producto = productoRepository
                .findByIdConTodo(id, store)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado")
                );

        return ProductoMapper.toDetail(producto);
    }
    
    @Transactional
    public void subirImagenesProducto(
            Long productoId,
            List<MultipartFile> imagenes,
            Store store
    ) {

        Producto producto =
                obtenerProducto(productoId, store);

        List<String> publicIds =
                new ArrayList<>();

        subirImagenesInterno(
                producto,
                imagenes,
                publicIds
        );
    }

    // ============================================================
    // LEGACY
    // ============================================================

    @Transactional
    public void eliminarImagenInmediatoSeguro(
            Long productoId,
            Long idImagen,
            Store store
    ) {

        Producto producto = productoRepository
                .findByIdConTodo(productoId, store)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        ImagenProducto img = imagenProductoRepository
                .findById(idImagen)
                .orElseThrow(() -> new ProductoNotFoundException(idImagen));

        if (!img.getProducto().getId().equals(producto.getId())) {
            throw new IllegalArgumentException(
                    "La imagen no pertenece al producto indicado"
            );
        }

        cloudinaryService.eliminarImagen(img.getPublicId());

        imagenProductoRepository.delete(img);
    }

    // ============================================================
    // HELPERS
    // ============================================================


    private void validarProducto(Producto producto) {

        if (producto.getProductName() == null || producto.getProductName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (producto.getPrice() == null || producto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio inválido");
        }
        

        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {

            boolean hayStockNegativo = producto.getVariantes().stream()
                    .anyMatch(v -> v.getStock() != null && v.getStock() < 0);

            if (hayStockNegativo) {
                throw new IllegalArgumentException("Stock inválido en variantes");
            }
        }
        
        if (
                producto.getVariantes() == null ||
                producto.getVariantes().isEmpty()
        ) {

            if (
                    producto.getStockSimple() == null ||
                    producto.getStockSimple() < 0
            ) {
                throw new IllegalArgumentException(
                        "Stock simple inválido"
                );
            }

            log.info(
                    "Producto simple validado id={}, stock={}",
                    producto.getId(),
                    producto.getStockSimple()
            );
        }
    }

    // ============================================================
    // EXCEPCIÓN
    // ============================================================

    public static class ProductoNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ProductoNotFoundException(Long id) {
            super("Producto no encontrado con ID: " + id);
        }
    }
   
    @Transactional(readOnly = true)
    public List<CategoriaGrupoCardDTO>
    obtenerProductosAgrupadosPorCategoria(
            Store store
    ) {

        List<Producto> productos =
                productoRepository
                        .findProductosVisiblesConTodo(store);

        return productos.stream()
                .collect(Collectors.groupingBy(
                        p -> new AbstractMap.SimpleEntry<>(
                                p.getCategoria().getId(),
                                p.getCategoria().getNombre()
                        ),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                ProductoMapper::toCard,
                                Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(e -> new CategoriaGrupoCardDTO(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        e.getValue()
                ))
                .toList();
    }

    @Transactional
    public void toggleVisibilidadPorCategoria(
            Long categoriaId,
            boolean visible,
            Store store
    ) {

        productoRepository
                .updateVisibilidadPorCategoria(
                        categoriaId,
                        visible,
                        store
                );
    }
    
    @Transactional
    public void actualizarPrecio(
            Long id,
            BigDecimal precio,
            Store store
    ) {

        if (precio == null ||
                precio.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Precio inválido"
            );
        }

        Producto producto =
                productoRepository
                        .findByIdConTodo(id, store)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Producto no encontrado"
                                )
                        );

        if (producto.getVariantes() != null &&
                !producto.getVariantes().isEmpty()) {

            throw new IllegalStateException(
                    "Producto con variantes no usa precio base"
            );
        }

        producto.setPrice(
                precio.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );
    }
    
    private BigDecimal calcularPrecioAjustado(
            BigDecimal precioActual,
            BigDecimal valor,
            String modo
    ) {
        BigDecimal nuevoPrecio;

        if ("MONTO".equalsIgnoreCase(modo)) {

            nuevoPrecio =
                    precioActual.add(valor);

        } else {

            BigDecimal porcentaje =
                    valor.divide(
                            BigDecimal.valueOf(100),
                            8,
                            RoundingMode.HALF_UP
                    );

            nuevoPrecio =
                    precioActual.add(
                            precioActual.multiply(porcentaje)
                    );
        }

        BigDecimal minimo =
                new BigDecimal("0.01");

        if (nuevoPrecio.compareTo(minimo) < 0) {
            nuevoPrecio = minimo;
        }

        return nuevoPrecio.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
    @Transactional
    public void ajustarPrecioCategoria(
            Long categoriaId,
            BigDecimal valor,
            String modo,
            Store store
    ) {
        if (categoriaId == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria"
            );
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "El valor debe ser distinto de cero"
            );
        }

        if (!"MONTO".equalsIgnoreCase(modo) &&
                !"PORCENTAJE".equalsIgnoreCase(modo)) {

            throw new IllegalArgumentException(
                    "Modo de ajuste inválido"
            );
        }

        categoriaService.obtenerPorId(
                categoriaId,
                store
        );

        List<Producto> productos =
                productoRepository.findByCategoriaIdAndStore(
                        categoriaId,
                        store
                );

        if (productos.isEmpty()) {
            throw new IllegalStateException(
                    "No hay productos en la categoría seleccionada"
            );
        }

        int productosActualizados = 0;
        int variantesActualizadas = 0;

        for (Producto producto : productos) {

            if (producto.getPrice() != null) {
                producto.setPrice(
                        calcularPrecioAjustado(
                                producto.getPrice(),
                                valor,
                                modo
                        )
                );

                productosActualizados++;
            }

            if (producto.getVariantes() != null) {

                for (ProductoVariante variante
                        : producto.getVariantes()) {

                    if (variante.getPrecio() == null) {
                        continue;
                    }

                    variante.setPrecio(
                            calcularPrecioAjustado(
                                    variante.getPrecio(),
                                    valor,
                                    modo
                            )
                    );

                    variantesActualizadas++;
                }
            }
        }

        productoRepository.saveAll(productos);

        log.info(
                "Ajuste masivo aplicado: storeId={}, categoriaId={}, " +
                "modo={}, valor={}, productos={}, variantes={}",
                store.getId(),
                categoriaId,
                modo,
                valor,
                productosActualizados,
                variantesActualizadas
        );
    }
    
    
    @Transactional
    public Producto crearProducto(
            ProductoAdminDTO dto,
            List<MultipartFile> imagenes,
            Store store
    )
    {

    	Producto producto = new Producto();

    	producto.setStore(store);
        // =====================================
        // DATOS BASE
        // =====================================
        producto.setProductName(
            dto.getProductName().trim()
        );

        producto.setPrice(
            dto.getPrecio()
        );

        producto.setDescription(
            dto.getDescription()
        );

        // =====================================
        // CATEGORÍA (POR NOMBRE)
        // =====================================
        if(dto.getCategoriaId()!=null){
            producto.setCategoria(
            		categoriaService.obtenerPorId(dto.getCategoriaId(), store)
            );
        }else if(dto.getNuevaCategoria()!=null &&
                 !dto.getNuevaCategoria().isBlank()){

            producto.setCategoria(
                categoriaService.obtenerOCrearCategoria(
                    dto.getNuevaCategoria().trim(),
                    store
                )
            );
        }

        // =====================================
        // MARCA (OPCIONAL)
        // =====================================
        if(dto.getMarcaId() != null){

            producto.setMarca(
                marcaService.obtenerPorId(dto.getMarcaId(),store)
            );

        }else if(dto.getMarcaNombre() != null &&
                 !dto.getMarcaNombre().isBlank()){

            producto.setMarca(
                marcaService.obtenerOCrear(
                    dto.getMarcaNombre().trim(), store
                )
            );
        }

        // =====================================
        // PROMOCIÓN
        // =====================================
        Double descuento =
            dto.getPorcentajeDescuento() != null
            ? dto.getPorcentajeDescuento()
            : 0.0;

        producto.setPorcentajeDescuento(
            descuento
        );

        producto.setTienePromocion(
            descuento > 0
        );

        // =====================================
        // VARIANTES
        // =====================================
        if(dto.getVariantes() != null &&
           !dto.getVariantes().isEmpty()){

            producto.setStockSimple(0);

            for(ProductoVarianteDTO vDto :
                dto.getVariantes()){

                if(vDto.getStock() == null &&
                   vDto.getPrecio() == null){
                    continue;
                }

                ProductoVariante variante =
                    new ProductoVariante();

                variante.setProducto(producto);

                variante.setStock(
                    vDto.getStock() != null
                    ? vDto.getStock()
                    : 0
                );

                variante.setPrecio(
                    vDto.getPrecio() != null
                    ? vDto.getPrecio()
                    : dto.getPrecio()
                );

                if(vDto.getAtributos() != null){

                    vDto.getAtributos()
                    .forEach((k,v) -> {

                        if(v != null &&
                           !v.isBlank()){

                            variante.agregarAtributo(
                                k.trim(),
                                v.trim()
                            );
                        }
                    });
                }

                producto.agregarVariante(
                    variante
                );
            }

        }else{

            producto.setStockSimple(
                dto.getStockSimple() != null
                ? dto.getStockSimple()
                : 0
            );
        }

        // =====================================
        // GUARDAR
        // =====================================
        Producto saved =
            productoRepository.save(producto);

        // =====================================
        // IMÁGENES
        // =====================================
        if(imagenes != null &&
           !imagenes.isEmpty()){

            subirImagenesProducto(
                saved.getId(),
                imagenes,
                store
            );
        }

        return saved;
    }
    
    @Transactional(readOnly = true)
    public List<ProductoCardDTO> obtenerProductosIndexOptimizado(Store store) {

        return productoRepository
                .findProductosIndexOptimizado(store);
    }
    @Transactional(readOnly = true)
    public ProductoAdminDTO obtenerProductoAdmin(
            Long id,
            Store store
    ) {

        Producto producto =
                productoRepository
                        .findByIdConTodo(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrado")
                        );

        return ProductoMapper.toAdmin(producto);
    }
    
    
    @Transactional(readOnly = true)
    public List<ProductoAdminListDTO> obtenerProductosAdminOptimizado(
            Store store
    ) {

        List<Producto> productosBase =
                productoRepository.findProductosAdminBase(store);

        List<Long> ids = productosBase.stream()
                .map(Producto::getId)
                .toList();

        List<Producto> productosConVariantes =
                productoRepository.findProductosConVariantes(ids, store);

        Map<Long, Producto> map = productosBase.stream()
                .collect(Collectors.toMap(
                        Producto::getId,
                        p -> p
                ));

        for (Producto p : productosConVariantes) {

            Producto base = map.get(p.getId());

            if (base != null) {
                base.setVariantes(p.getVariantes());
            }
        }

        return productosBase.stream()
                .map(ProductoAdminListMapper::toDTO)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaGrupoAdminDTO>
    obtenerProductosAdminAgrupados(
            Store store
    ) {

        List<ProductoAdminListDTO> productos =
                obtenerProductosAdminOptimizado(store);

        return productos.stream()
                .collect(Collectors.groupingBy(
                        p -> new AbstractMap.SimpleEntry<>(
                                p.getCategoriaId(),
                                p.getCategoriaNombre()
                        ),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(e -> new CategoriaGrupoAdminDTO(
                        e.getKey().getKey(),
                        e.getKey().getValue(),
                        e.getValue()
                ))
                .toList();
    }
    
  

}
