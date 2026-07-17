CREATE TABLE inventory_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,

    store_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    variante_id BIGINT NULL,
    order_id BIGINT NULL,

    type VARCHAR(40) NOT NULL,

    quantity INT NOT NULL,
    stock_before INT NOT NULL,
    stock_after INT NOT NULL,

    reason VARCHAR(500) NULL,

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_inventory_store_created (
        store_id,
        created_at
    ),

    INDEX idx_inventory_product_created (
        producto_id,
        created_at
    ),

    INDEX idx_inventory_variant_created (
        variante_id,
        created_at
    ),

    INDEX idx_inventory_order (
        order_id
    ),

    CONSTRAINT fk_inventory_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (producto_id)
        REFERENCES productos (id),

    CONSTRAINT fk_inventory_variant
        FOREIGN KEY (variante_id)
        REFERENCES producto_variantes (id),

    CONSTRAINT fk_inventory_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
);