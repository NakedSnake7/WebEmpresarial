
package com.webempresarial.store.model;

import java.math.BigDecimal;   
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "producto_variantes")
public class ProductoVariante {
	
	@Column(nullable = false)
	private Boolean principal = false;
	
	

	public String getNombreVisual() {
	    Map<String,String> map = getAtributosMap();

	    return map.getOrDefault("Color","") +
	           " " +
	           map.getOrDefault("Talla","");
	}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con producto base
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @Min(0)
    private Integer stock = 0;

    @DecimalMin("0.0")
    private BigDecimal precio;
    

    // 🔥 ATRIBUTOS DINÁMICOS (lo importante del cambio)
    @OneToMany(
    	    mappedBy = "variante",
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true,
    	    fetch = FetchType.LAZY
    	)
    	private Set<VarianteAtributo> atributos = new LinkedHashSet<>();
    
    
    
    public void addAtributo(VarianteAtributo atributo) {
        atributo.setVariante(this);
        atributos.add(atributo);
    }
    
    
    public Map<String, String> getAtributosMap() {
        return atributos.stream()
            .collect(Collectors.toMap(
                VarianteAtributo::getNombre,
                VarianteAtributo::getValor
            ));
    }
    
    @PrePersist
    @PreUpdate
    private void validar() {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock inválido");
        }
    }

    // -----------------------------
    // HELPERS
    // -----------------------------
    public void agregarAtributo(VarianteAtributo attr) {
        attr.setVariante(this);
        this.atributos.add(attr);
    }
    
    public BigDecimal getPrecioFinal() {
        return precio != null ? precio : producto.getPrice();
    }

    public void agregarAtributo(String nombre, String valor) {
        VarianteAtributo attr = new VarianteAtributo();
        attr.setNombre(nombre);
        attr.setValor(valor);
        attr.setVariante(this);
        atributos.add(attr);
    }

    // -----------------------------
    // GETTERS / SETTERS
    // -----------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

  
    public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Set<VarianteAtributo> getAtributos() {
	    return atributos;
	}
	    
	public void setAtributos(Set<VarianteAtributo> atributos) {
	    this.atributos.clear();

	    if (atributos != null) {
	        for (VarianteAtributo attr : atributos) {
	            attr.setVariante(this);
	            this.atributos.add(attr);
	        }
	    }
	}
}