CREATE TABLE order_audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,

    action VARCHAR(40) NOT NULL,

    previous_order_status VARCHAR(40) NULL,
    new_order_status VARCHAR(40) NULL,

    previous_payment_status VARCHAR(40) NULL,
    new_payment_status VARCHAR(40) NULL,

    actor_username VARCHAR(160) NULL,
    actor_type VARCHAR(40) NULL,

    reason VARCHAR(500) NULL,

    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_order_audit_order (
        order_id,
        created_at
    ),

    INDEX idx_order_audit_store (
        store_id,
        created_at
    ),

    CONSTRAINT fk_order_audit_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id),

    CONSTRAINT fk_order_audit_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id)
);