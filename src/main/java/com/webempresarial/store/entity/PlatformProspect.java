package com.webempresarial.store.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "platform_prospects")
public class PlatformProspect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String whatsapp;

    @Column(length = 120)
    private String empresa;

    @Column(length = 150)
    private String instagram;

    @Column(length = 120)
    private String servicio;

    @Column(length = 120)
    private String presupuesto;

    @Column(length = 2000)
    private String objetivo;

    @Column(name = "cohort_hint", nullable = false, length = 12)
    private String cohortHint;

    @Column(name = "payment_preference", nullable = false, length = 20)
    private String paymentPreference;

    @Column(name = "source", nullable = false, length = 24)
    private String source;

    @Column(nullable = false, length = 16)
    private String status = "NEW";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    protected PlatformProspect() {}

    public PlatformProspect(String nombre, String whatsapp, String empresa,
            String instagram, String servicio, String presupuesto,
            String objetivo, String cohortHint, String paymentPreference,
            String source) {
        this.nombre = nombre;
        this.whatsapp = whatsapp;
        this.empresa = empresa;
        this.instagram = instagram;
        this.servicio = servicio;
        this.presupuesto = presupuesto;
        this.objetivo = objetivo;
        this.cohortHint = cohortHint;
        this.paymentPreference = paymentPreference;
        this.source = source;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getWhatsapp() { return whatsapp; }
    public String getCohortHint() { return cohortHint; }
    public String getPaymentPreference() { return paymentPreference; }
    public String getSource() { return source; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
