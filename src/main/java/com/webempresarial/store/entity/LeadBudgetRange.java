package com.webempresarial.store.entity;

import com.webempresarial.store.model.Store;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "lead_budget_ranges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_range_store_code",
                        columnNames = {"store_id", "code"}
                )
        }
)
public class LeadBudgetRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer scoreWeight = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public Integer getScoreWeight() {
        return scoreWeight;
    }

    public void setScoreWeight(Integer scoreWeight) {
        this.scoreWeight = scoreWeight;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}