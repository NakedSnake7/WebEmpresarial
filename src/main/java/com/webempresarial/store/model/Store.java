package com.webempresarial.store.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stores",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_store_dominio",
            columnNames = "dominio"
        )
    }
)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false, unique = true)
    private String dominio;

    @Column(nullable = false)
    private boolean activa = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorePlan plan = StorePlan.BASIC;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    public StorePlan getPlan() {
        return plan;
    }

    public void setPlan(StorePlan plan) {
        this.plan = plan;
    }
}