package com.webempresarial.store.entity;

import java.time.LocalDateTime;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "feature_usage")
public class FeatureUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Feature feature;

    @Column(length = 120)
    private String context;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    public void prePersist() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Feature getFeature() { return feature; }
    public void setFeature(Feature feature) { this.feature = feature; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}