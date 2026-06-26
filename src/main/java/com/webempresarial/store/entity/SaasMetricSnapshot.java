package com.webempresarial.store.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "saas_metric_snapshots")
public class SaasMetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate snapshotDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrr = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal arr = BigDecimal.ZERO;

    private Long activeSubscriptions = 0L;

    private Long activeStores = 0L;

    @PrePersist
    public void prePersist() {
        if (snapshotDate == null) {
            snapshotDate = LocalDate.now();
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(LocalDate snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public BigDecimal getMrr() {
		return mrr;
	}

	public void setMrr(BigDecimal mrr) {
		this.mrr = mrr;
	}

	public BigDecimal getArr() {
		return arr;
	}

	public void setArr(BigDecimal arr) {
		this.arr = arr;
	}

	public Long getActiveSubscriptions() {
		return activeSubscriptions;
	}

	public void setActiveSubscriptions(Long activeSubscriptions) {
		this.activeSubscriptions = activeSubscriptions;
	}

	public Long getActiveStores() {
		return activeStores;
	}

	public void setActiveStores(Long activeStores) {
		this.activeStores = activeStores;
	}

}