CREATE TABLE order_outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,

    order_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    expiration_date DATETIME(6) NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,

    next_attempt_at DATETIME(6) NULL,
    locked_at DATETIME(6) NULL,
    processed_at DATETIME(6) NULL,

    idempotency_key VARCHAR(150) NOT NULL,
    last_error TEXT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_order_outbox_idempotency
        UNIQUE (idempotency_key),

    INDEX idx_order_outbox_pending (
        status,
        next_attempt_at,
        created_at
    ),

    INDEX idx_order_outbox_order (
        order_id
    ),

    CONSTRAINT fk_order_outbox_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id),

    CONSTRAINT fk_order_outbox_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id)
);