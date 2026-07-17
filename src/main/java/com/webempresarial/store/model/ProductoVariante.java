package com.webempresarial.store.model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "producto_variantes")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @Min(0)
    private Integer stock = 0;

    @DecimalMin("0.0")
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean principal = false;

    @OneToMany(
            mappedBy = "variante",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<VarianteAtributo> atributos =
            new LinkedHashSet<>();

    // =============================
    // HELPERS
    // =============================

    @Transient
    public String getNombreVisual() {

        if (atributos == null || atributos.isEmpty()) {
            return id != null
                    ? "Variante #" + id
                    : "Variante";
        }

        String nombre = atributos.stream()
                .filter(atributo ->
                        atributo.getNombre() != null
                                && !atributo.getNombre().isBlank()
                                && atributo.getValor() != null
                                && !atributo.getValor().isBlank()
                )
                .sorted(
                        Comparator.comparing(
                                VarianteAtributo::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(atributo ->
                        atributo.getNombre().trim()
                                + ": "
                                + atributo.getValor().trim()
                )
                .collect(Collectors.joining(" · "));

        return nombre.isBlank()
                ? id != null
                    ? "Variante #" + id
                    : "Variante"
                : nombre;
    }

    @Transient
    public Map<String, String> getAtributosMap() {

        if (atributos == null || atributos.isEmpty()) {
            return Map.of();
        }

        return atributos.stream()
                .filter(atributo ->
                        atributo.getNombre() != null
                                && atributo.getValor() != null
                )
                .collect(Collectors.toMap(
                        VarianteAtributo::getNombre,
                        VarianteAtributo::getValor,
                        (valorAnterior, valorNuevo) -> valorNuevo,
                        LinkedHashMap::new
                ));
    }

    public void addAtributo(
            VarianteAtributo atributo
    ) {
        if (atributo == null) {
            return;
        }

        atributo.setVariante(this);
        atributos.add(atributo);
    }

    public void agregarAtributo(
            VarianteAtributo atributo
    ) {
        addAtributo(atributo);
    }

    public void agregarAtributo(
            String nombre,
            String valor
    ) {
        VarianteAtributo atributo =
                new VarianteAtributo();

        atributo.setNombre(nombre);
        atributo.setValor(valor);
        atributo.setVariante(this);

        atributos.add(atributo);
    }

    public BigDecimal getPrecioFinal() {
        return precio != null
                ? precio
                : producto != null
                    ? producto.getPrice()
                    : BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    private void validar() {

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException(
                    "Stock inválido"
            );
        }

        if (principal == null) {
            principal = false;
        }
    }

    // =============================
    // GETTERS / SETTERS
    // =============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(
            Producto producto
    ) {
        this.producto = producto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(
            Integer stock
    ) {
        this.stock = stock;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(
            BigDecimal precio
    ) {
        this.precio = precio;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(
            Boolean principal
    ) {
        this.principal = principal;
    }

    public Set<VarianteAtributo> getAtributos() {
        return atributos;
    }

    public void setAtributos(
            Set<VarianteAtributo> atributos
    ) {
        this.atributos.clear();

        if (atributos == null) {
            return;
        }

        atributos.forEach(this::addAtributo);
    }
}