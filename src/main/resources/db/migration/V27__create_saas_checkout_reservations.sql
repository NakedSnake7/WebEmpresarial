CREATE TABLE saas_checkout_reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,

    attempt_id VARCHAR(36) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    plan VARCHAR(30) NOT NULL,

    stripe_session_id VARCHAR(100) NULL,

    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_saas_checkout_reservation_attempt
        UNIQUE (attempt_id),

    CONSTRAINT uk_saas_checkout_reservation_domain
        UNIQUE (domain),

    CONSTRAINT uk_saas_checkout_reservation_email
        UNIQUE (email),

    INDEX idx_saas_checkout_reservation_expires_at (expires_at)
);
