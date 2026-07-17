CREATE TABLE inventory_alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    store_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    variante_id BIGINT NULL,

    level VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    current_stock INT NOT NULL,
    stock_threshold INT NOT NULL,

    active_key VARCHAR(160) NULL,
    occurrence_count INT NOT NULL DEFAULT 1,

    first_detected_at DATETIME(6) NOT NULL,
    last_detected_at DATETIME(6) NOT NULL,

    acknowledged_at DATETIME(6) NULL,
    acknowledged_by VARCHAR(150) NULL,

    resolved_at DATETIME(6) NULL,
    resolution_note VARCHAR(500) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_inventory_alert_active_key
        UNIQUE (active_key),

    INDEX idx_inventory_alert_store_status (
        store_id,
        status,
        level
    ),

    INDEX idx_inventory_alert_product (
        producto_id,
        status
    ),

    INDEX idx_inventory_alert_variant (
        variante_id,
        status
    ),

    INDEX idx_inventory_alert_detected (
        store_id,
        last_detected_at
    ),

    CONSTRAINT fk_inventory_alert_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id),

    CONSTRAINT fk_inventory_alert_product
        FOREIGN KEY (producto_id)
        REFERENCES productos (id),

    CONSTRAINT fk_inventory_alert_variant
        FOREIGN KEY (variante_id)
        REFERENCES producto_variantes (id)
);