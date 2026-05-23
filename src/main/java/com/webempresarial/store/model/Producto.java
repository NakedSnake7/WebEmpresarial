package com.webempresarial.store.model;

import java.math.BigDecimal;  
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "productos")
public class Producto {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;
	
	@Version
	private Long version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_simple")
    private Integer stockSimple = 0;
	
	private String sku;
	

	// 🔥 CAMBIO IMPORTANTE: LAZY para evitar N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="marca_id")
    private Marca marca;

   

	@Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 🔥 CAMBIO IMPORTANTE: LAZY para evitar N+1
	@OneToMany(
		    mappedBy = "producto",
		    cascade = CascadeType.ALL,
		    orphanRemoval = true,
		    fetch = FetchType.LAZY
		)
		private Set<ImagenProducto> imagenes = new LinkedHashSet<>();
    
    @OneToMany(
    	    mappedBy = "producto",
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true,
    	    fetch = FetchType.LAZY
    	)
    	private Set<ProductoVariante> variantes = new LinkedHashSet<>();
    
    @Column(nullable = false)
    private boolean visibleEnMenu = true;

    @Column(name = "tiene_promocion", nullable = false)
    private Boolean tienePromocion = false;

    @Column(name = "porcentaje_descuento", nullable = false)
    private Double porcentajeDescuento = 0.0;
    
    

    
    public void agregarVariante(ProductoVariante variante) {
        if (variante == null) return;

        variante.setProducto(this);

        if (variante.getStock() == null) {
            variante.setStock(0);
        }

        this.variantes.add(variante);
    }
    
    public ProductoVariante getVariantePrincipal() {
        if (variantes == null || variantes.isEmpty()) return null;

        return variantes.stream()
                .filter(v -> Boolean.TRUE.equals(v.getPrincipal()))
                .findFirst()
                .orElse(variantes.stream().findFirst().orElse(null)); // fallback seguro
    }
    
    public int getStockDisponible() {

        // Si tiene variantes
        if (variantes != null && !variantes.isEmpty()) {
            return variantes.stream()
                    .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                    .sum();
        }

        // Producto simple sin variantes
        return stockSimple != null ? stockSimple : 0;
    }
    
    public boolean tieneVariantes() {
        return variantes != null && !variantes.isEmpty();
    }

    public String getImageUrl() {
        if (imagenes != null && !imagenes.isEmpty()) {
            ImagenProducto img = imagenes.stream().findFirst().orElse(null);
            if (img != null && img.getImageUrl() != null) {
                return img.getImageUrl();
            }
        }
        return "/img/default.png";
    }

    

    // 🔥 Calcula descuento seguro
    public BigDecimal getPrecioConDescuento() {

        if (Boolean.TRUE.equals(tienePromocion)
                && porcentajeDescuento != null
                && porcentajeDescuento > 0) {

            BigDecimal descuento = price
                    .multiply(BigDecimal.valueOf(porcentajeDescuento))
                    .divide(BigDecimal.valueOf(100));

            return price.subtract(descuento);
        }

        return price;
    }
    
    public BigDecimal getPrecioMinimo() {

        if (tieneVariantes()) {
            return variantes.stream()
                    .map(v -> v.getPrecio() != null ? v.getPrecio() : this.price)
                    .min(BigDecimal::compareTo)
                    .orElse(this.price);
        }

        return this.price;
    }
    
    public boolean tieneStock() {
        return getStockDisponible() > 0;
    }


    // Constructor vacío
    public Producto() {}

    // Constructor útil
    public Producto(String productName, BigDecimal price, String description, Categoria categoria) {
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.categoria = categoria;
    }


    // Método para actualizar productos
    public void actualizarDatosDesde(Producto producto, Categoria categoria) {
        this.productName = producto.getProductName();
        this.price = producto.getPrice();
        this.description = producto.getDescription();
        this.categoria = categoria;
    }


    // Getters y Setters ------------------

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
    }

    public Boolean getTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(Boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
    
    

    public void setImagenes(Set<ImagenProducto> imagenes) {
        this.imagenes = imagenes;
    }

    public void setVariantes(Set<ProductoVariante> variantes) {
        this.variantes = variantes;
    }

    public Set<ImagenProducto> getImagenes() {
        return imagenes;
    }

    public Set<ProductoVariante> getVariantes() {
        return variantes;
    }

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}


	public Integer getStockSimple() {
	    return stockSimple;
	}

	public void setStockSimple(Integer stockSimple) {
	    this.stockSimple = stockSimple;
	}
	 public Marca getMarca() {
			return marca;
		}

		public void setMarca(Marca marca) {
			this.marca = marca;
		}
		public Store getStore() {
		    return store;
		}

		public void setStore(Store store) {
		    this.store = store;
		}
}
