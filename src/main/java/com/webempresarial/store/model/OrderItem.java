package com.webempresarial.store.model;

import java.math.BigDecimal; 

import jakarta.persistence.*;  
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_producto", columnList = "producto_id"),
        @Index(name = "idx_order_item_variante", columnList = "variante_id")
    }
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;  // Relación con la entidad Producto

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "El precio no puede ser nulo")
    @Min(value = 0, message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = true)
    private ProductoVariante variante;
    
    @Column(name = "product_name")
    private String productName;

    // Constructor vacío para JPA
    public OrderItem() {}

    // Constructor con parámetros
    public OrderItem(Producto producto, Integer quantity,BigDecimal price, Order order) {
        this.producto = producto;
        this.quantity = quantity;
        this.price = price;
        this.order = order;
    }

    public BigDecimal getSubtotal() {

        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        return price.multiply(BigDecimal.valueOf(quantity.longValue()));
    }
    
    // Getters y setters
    
    public Long getId() {
        return id;
    }

    public ProductoVariante getVariante() {
		return variante;
	}

	public void setVariante(ProductoVariante variante) {
		this.variante = variante;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
