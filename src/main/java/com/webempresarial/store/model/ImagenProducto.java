package com.webempresarial.store.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "imagenes_productos",
    indexes = {
        @Index(name = "idx_imagen_producto", columnList = "producto_id")
    }
)
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "orden")
    private Integer orden = 0;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    public ImagenProducto() {}

    public ImagenProducto(String imageUrl, String publicId, Producto producto) {
        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.producto = producto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
}