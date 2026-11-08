CREATE TABLE pilot_offers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    lead_id BIGINT NOT NULL,

    cohort VARCHAR(1) NOT NULL,
    initial_amount_mxn DECIMAL(12,2) NOT NULL,
    renewal_amount_mxn DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'MXN',

    status VARCHAR(24) NOT NULL DEFAULT 'DRAFT',
    payment_method VARCHAR(16) NULL,
    terms_version VARCHAR(32) NOT NULL,

    stripe_checkout_session_id VARCHAR(255) NULL,
    stripe_payment_intent_id VARCHAR(255) NULL,

    offered_at DATETIME(6) NULL,
    accepted_at DATETIME(6) NULL,
    paid_at DATETIME(6) NULL,
    expires_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_pilot_offers_store
        FOREIGN KEY (store_id) REFERENCES `stores`(id),

    CONSTRAINT fk_pilot_offers_lead
        FOREIGN KEY (lead_id) REFERENCES `leads`(id),

    CONSTRAINT uk_pilot_offers_stripe_session
        UNIQUE (stripe_checkout_session_id),

    CONSTRAINT uk_pilot_offers_stripe_intent
        UNIQUE (stripe_payment_intent_id),

    CONSTRAINT ck_pilot_offers_amount
        CHECK (
            (cohort = 'A' AND initial_amount_mxn = 990.00)
            OR
            (cohort = 'B' AND initial_amount_mxn = 1490.00)
        ),

    CONSTRAINT ck_pilot_offers_renewal
        CHECK (renewal_amount_mxn = 790.00),

    CONSTRAINT ck_pilot_offers_currency
        CHECK (currency = 'MXN')
);

CREATE INDEX idx_pilot_offers_store_status
    ON pilot_offers (store_id, status);

CREATE INDEX idx_pilot_offers_lead
    ON pilot_offers (lead_id);
