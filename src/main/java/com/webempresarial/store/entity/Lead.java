package com.webempresarial.store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.webempresarial.store.model.LeadPriority;
import com.webempresarial.store.model.LeadStatus;
import com.webempresarial.store.model.LeadTemperature;
import com.webempresarial.store.model.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // STORE / TENANT
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // =========================
    // DATOS DEL LEAD
    // =========================
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String whatsapp;

    @Column(length = 120)
    private String empresa;

    @Column(length = 150)
    private String instagram;

    @Column(nullable = false, length = 50)
    private String servicio;

    @Column(nullable = false, length = 50)
    private String presupuesto;

    @Column(columnDefinition = "TEXT")
    private String objetivo;

    @Column(length = 100)
    private String source;

    @Column(length = 150)
    private String exactSource;

    // =========================
    // CRM COMERCIAL
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadTemperature temperature = LeadTemperature.COLD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadPriority priority = LeadPriority.MEDIUM;

    @Column(nullable = false)
    private Integer score = 0;

    private BigDecimal projectedValue;

    private BigDecimal proposalAmount;

    private Integer closeProbability;

    private LocalDateTime lastContactAt;

    private LocalDateTime nextFollowUpAt;

    // =========================
    // TIMESTAMPS
    // =========================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    private LocalDateTime lostAt;

    // =========================
    // RELACIONES FUTURAS
    // =========================
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeadActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesTask> tasks = new ArrayList<>();

    // =========================
    // LIFECYCLE
    // =========================
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) this.status = LeadStatus.NEW;
        if (this.temperature == null) this.temperature = LeadTemperature.COLD;
        if (this.priority == null) this.priority = LeadPriority.MEDIUM;
        if (this.score == null) this.score = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.status == LeadStatus.CLOSED && this.closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }

        if (this.status == LeadStatus.LOST && this.lostAt == null) {
            this.lostAt = LocalDateTime.now();
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getWhatsapp() {
		return whatsapp;
	}

	public void setWhatsapp(String whatsapp) {
		this.whatsapp = whatsapp;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getInstagram() {
		return instagram;
	}

	public void setInstagram(String instagram) {
		this.instagram = instagram;
	}

	public String getServicio() {
		return servicio;
	}

	public void setServicio(String servicio) {
		this.servicio = servicio;
	}

	public String getPresupuesto() {
		return presupuesto;
	}

	public void setPresupuesto(String presupuesto) {
		this.presupuesto = presupuesto;
	}

	public String getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(String objetivo) {
		this.objetivo = objetivo;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getExactSource() {
		return exactSource;
	}

	public void setExactSource(String exactSource) {
		this.exactSource = exactSource;
	}

	public LeadStatus getStatus() {
		return status;
	}

	public void setStatus(LeadStatus status) {
		this.status = status;
	}

	public LeadTemperature getTemperature() {
		return temperature;
	}

	public void setTemperature(LeadTemperature temperature) {
		this.temperature = temperature;
	}

	public LeadPriority getPriority() {
		return priority;
	}

	public void setPriority(LeadPriority priority) {
		this.priority = priority;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public BigDecimal getProjectedValue() {
		return projectedValue;
	}

	public void setProjectedValue(BigDecimal projectedValue) {
		this.projectedValue = projectedValue;
	}

	public BigDecimal getProposalAmount() {
		return proposalAmount;
	}

	public void setProposalAmount(BigDecimal proposalAmount) {
		this.proposalAmount = proposalAmount;
	}

	public Integer getCloseProbability() {
		return closeProbability;
	}

	public void setCloseProbability(Integer closeProbability) {
		this.closeProbability = closeProbability;
	}

	public LocalDateTime getLastContactAt() {
		return lastContactAt;
	}

	public void setLastContactAt(LocalDateTime lastContactAt) {
		this.lastContactAt = lastContactAt;
	}

	public LocalDateTime getNextFollowUpAt() {
		return nextFollowUpAt;
	}

	public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) {
		this.nextFollowUpAt = nextFollowUpAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.closedAt = closedAt;
	}

	public LocalDateTime getLostAt() {
		return lostAt;
	}

	public void setLostAt(LocalDateTime lostAt) {
		this.lostAt = lostAt;
	}

	public List<LeadActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<LeadActivity> activities) {
		this.activities = activities;
	}

	public List<SalesTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SalesTask> tasks) {
		this.tasks = tasks;
	}

    // getters y setters...
    
}