package com.webempresarial.store.dto.producto.admin;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.webempresarial.store.dto.producto.shared.ProductoVarianteDTO;

public class ProductoAdminDTO {

    private Long id;

    private String productName;
    private String description;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "El precio base no puede ser negativo"
    )
    private BigDecimal precio;

    private Long marcaId;
    private String marcaNombre;

    private Long categoriaId;
    private String nuevaCategoria;

    private Integer stockSimple;

    private boolean visibleEnMenu;

    private Double porcentajeDescuento;
    private boolean tienePromocion;

    private List<Long> imagenesExistentes;
    private List<Long> imagenesEliminar;

    // NUEVO
    private List<String> urlsImagenesExistentes;

    private List<ProductoVarianteDTO> variantes;
    
    public String getImagenPrincipal() {
        if (urlsImagenesExistentes != null && !urlsImagenesExistentes.isEmpty()) {
            return urlsImagenesExistentes.get(0);
        }
        return "/img/default.jpg";
    }
    

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Long getMarcaId() {
        return marcaId;
    }

    public void setMarcaId(Long marcaId) {
        this.marcaId = marcaId;
    }

    public String getMarcaNombre() {
        return marcaNombre;
    }

    public void setMarcaNombre(String marcaNombre) {
        this.marcaNombre = marcaNombre;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getNuevaCategoria() {
        return nuevaCategoria;
    }

    public void setNuevaCategoria(String nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }

    public Integer getStockSimple() {
        return stockSimple;
    }

    public void setStockSimple(Integer stockSimple) {
        this.stockSimple = stockSimple;
    }

    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public boolean isTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public List<Long> getImagenesExistentes() {
        return imagenesExistentes;
    }

    public void setImagenesExistentes(List<Long> imagenesExistentes) {
        this.imagenesExistentes = imagenesExistentes;
    }

    public List<Long> getImagenesEliminar() {
        return imagenesEliminar;
    }

    public void setImagenesEliminar(List<Long> imagenesEliminar) {
        this.imagenesEliminar = imagenesEliminar;
    }

    public List<String> getUrlsImagenesExistentes() {
        return urlsImagenesExistentes;
    }

    public void setUrlsImagenesExistentes(List<String> urlsImagenesExistentes) {
        this.urlsImagenesExistentes = urlsImagenesExistentes;
    }

    public List<ProductoVarianteDTO> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<ProductoVarianteDTO> variantes) {
        this.variantes = variantes;
    }
    public boolean isTieneVariantes() {
        return variantes != null && !variantes.isEmpty();
    }
}