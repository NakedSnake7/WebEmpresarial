-- ============================================================
-- V19 - Align Flyway schema with the current JPA model
--
-- Supports both:
--   1. Fresh databases created from V1 -> V18.
--   2. Existing databases previously evolved by Hibernate
--      ddl-auto=update.
-- ============================================================


-- ============================================================
-- orders.total
-- JPA: BigDecimal -> DECIMAL(38,2)
-- Historical V1: DOUBLE
-- ============================================================

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'orders'
              AND COLUMN_NAME = 'total'
              AND DATA_TYPE = 'decimal'
              AND NUMERIC_PRECISION = 38
              AND NUMERIC_SCALE = 2
              AND IS_NULLABLE = 'NO'
        ),
        'SELECT 1',
        'ALTER TABLE orders MODIFY COLUMN total DECIMAL(38,2) NOT NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ============================================================
-- Stripe webhook idempotency / processing registry
-- Missing from V1 -> V18 but required by StripeWebhookEvent.
-- ============================================================

CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    error_message TEXT COLLATE utf8mb4_unicode_ci NULL,
    event_type VARCHAR(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    processed_at DATETIME(6) DEFAULT NULL,
    received_at DATETIME(6) DEFAULT NULL,
    status ENUM('FAILED','PROCESSED','PROCESSING')
        COLLATE utf8mb4_unicode_ci NOT NULL,
    stripe_event_id VARCHAR(120)
        COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stripe_webhook_event_id (stripe_event_id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- subscriptions.pending_plan
-- ============================================================

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'subscriptions'
              AND COLUMN_NAME = 'pending_plan'
        ),
        'SELECT 1',
        'ALTER TABLE subscriptions
            ADD COLUMN pending_plan
            ENUM(''BASIC'',''PREMIUM'',''PRO'')
            COLLATE utf8mb4_unicode_ci NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ============================================================
-- subscriptions.pending_plan_effective_at
-- ============================================================

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'subscriptions'
              AND COLUMN_NAME = 'pending_plan_effective_at'
        ),
        'SELECT 1',
        'ALTER TABLE subscriptions
            ADD COLUMN pending_plan_effective_at DATETIME(6) NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ============================================================
-- subscriptions.cancel_at_period_end
-- ============================================================

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'subscriptions'
              AND COLUMN_NAME = 'cancel_at_period_end'
        ),
        'SELECT 1',
        'ALTER TABLE subscriptions
            ADD COLUMN cancel_at_period_end BIT(1) NULL DEFAULT b''0'''
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- Protect historical rows before enforcing NOT NULL.
UPDATE subscriptions
SET cancel_at_period_end = b'0'
WHERE cancel_at_period_end IS NULL;


SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'subscriptions'
              AND COLUMN_NAME = 'cancel_at_period_end'
              AND COLUMN_TYPE = 'bit(1)'
              AND IS_NULLABLE = 'NO'
        ),
        'SELECT 1',
        'ALTER TABLE subscriptions
            MODIFY COLUMN cancel_at_period_end BIT(1) NOT NULL'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
