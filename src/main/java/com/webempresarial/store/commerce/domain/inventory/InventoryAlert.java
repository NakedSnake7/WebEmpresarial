package com.webempresarial.store.commerce.domain.inventory;

import java.time.LocalDateTime; 

import com.webempresarial.store.commerce.domain.inventory.InventoryAlertLevel;
import com.webempresarial.store.commerce.domain.inventory.InventoryAlertStatus;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inventory_alerts",
        indexes = {
                @Index(
                        name = "idx_inventory_alert_store_status",
                        columnList = "store_id,status,level"
                ),
                @Index(
                        name = "idx_inventory_alert_product",
                        columnList = "producto_id,status"
                ),
                @Index(
                        name = "idx_inventory_alert_variant",
                        columnList = "variante_id,status"
                ),
                @Index(
                        name = "idx_inventory_alert_detected",
                        columnList = "store_id,last_detected_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_alert_active_key",
                        columnNames = "active_key"
                )
        }
)
public class InventoryAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "store_id",
            nullable = false
    )
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "producto_id",
            nullable = false
    )
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id")
    private ProductoVariante variante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryAlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryAlertStatus status;

    @Column(
            name = "current_stock",
            nullable = false
    )
    private Integer currentStock;

    @Column(
            name = "stock_threshold",
            nullable = false
    )
    private Integer stockThreshold;

    /*
     * Solo permanece informado mientras la alerta está activa.
     * Cuando se resuelve pasa a null, permitiendo una alerta futura
     * para el mismo producto o variante.
     */
    @Column(
            name = "active_key",
            length = 160
    )
    private String activeKey;

    @Column(
            name = "occurrence_count",
            nullable = false
    )
    private Integer occurrenceCount = 1;

    @Column(
            name = "first_detected_at",
            nullable = false
    )
    private LocalDateTime firstDetectedAt;

    @Column(
            name = "last_detected_at",
            nullable = false
    )
    private LocalDateTime lastDetectedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by", length = 150)
    private String acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = InventoryAlertStatus.OPEN;
        }

        if (occurrenceCount == null || occurrenceCount < 1) {
            occurrenceCount = 1;
        }

        if (firstDetectedAt == null) {
            firstDetectedAt = now;
        }

        if (lastDetectedAt == null) {
            lastDetectedAt = now;
        }
    }

    public void refresh(
            InventoryAlertLevel newLevel,
            int newStock,
            int threshold
    ) {
        boolean changed =
                level != newLevel
                || currentStock == null
                || currentStock.intValue() != newStock;

        this.level = newLevel;
        this.currentStock = newStock;
        this.stockThreshold = threshold;

        if (changed) {
            this.lastDetectedAt = LocalDateTime.now();
            this.occurrenceCount =
                    occurrenceCount == null
                            ? 1
                            : occurrenceCount + 1;
        }

        if (status == InventoryAlertStatus.RESOLVED) {
            status = InventoryAlertStatus.OPEN;
            resolvedAt = null;
            resolutionNote = null;
        }
    }

    public void acknowledge(String username) {
        if (status == InventoryAlertStatus.RESOLVED) {
            throw new IllegalStateException(
                    "No se puede reconocer una alerta resuelta"
            );
        }

        status = InventoryAlertStatus.ACKNOWLEDGED;
        acknowledgedAt = LocalDateTime.now();
        acknowledgedBy = username;
    }

    public void resolve(String note) {
        status = InventoryAlertStatus.RESOLVED;
        resolvedAt = LocalDateTime.now();
        resolutionNote = note;
        activeKey = null;
    }

    public boolean isActive() {
        return status == InventoryAlertStatus.OPEN
                || status == InventoryAlertStatus.ACKNOWLEDGED;
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public ProductoVariante getVariante() {
        return variante;
    }

    public void setVariante(ProductoVariante variante) {
        this.variante = variante;
    }

    public InventoryAlertLevel getLevel() {
        return level;
    }

    public void setLevel(InventoryAlertLevel level) {
        this.level = level;
    }

    public InventoryAlertStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryAlertStatus status) {
        this.status = status;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getStockThreshold() {
        return stockThreshold;
    }

    public void setStockThreshold(Integer stockThreshold) {
        this.stockThreshold = stockThreshold;
    }

    public String getActiveKey() {
        return activeKey;
    }

    public void setActiveKey(String activeKey) {
        this.activeKey = activeKey;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public LocalDateTime getFirstDetectedAt() {
        return firstDetectedAt;
    }

    public LocalDateTime getLastDetectedAt() {
        return lastDetectedAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }
}