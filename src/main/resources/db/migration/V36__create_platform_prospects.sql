-- Leads comerciales de plataforma, independientes de tiendas tenant.
-- cohort_hint NO establece precio contractual ni oferta.
CREATE TABLE platform_prospects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    whatsapp VARCHAR(40) NOT NULL,
    empresa VARCHAR(120) NULL,
    instagram VARCHAR(150) NULL,
    servicio VARCHAR(120) NULL,
    presupuesto VARCHAR(120) NULL,
    objetivo VARCHAR(2000) NULL,
    cohort_hint VARCHAR(12) NOT NULL,
    payment_preference VARCHAR(20) NOT NULL,
    source VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_platform_prospects_created_at (created_at),
    INDEX idx_platform_prospects_whatsapp (whatsapp),
    CONSTRAINT ck_platform_prospects_cohort
        CHECK (cohort_hint IN ('A', 'B', 'UNASSIGNED')),
    CONSTRAINT ck_platform_prospects_payment
        CHECK (payment_preference IN ('por-definir', 'transferencia', 'stripe')),
    CONSTRAINT ck_platform_prospects_source
        CHECK (source IN ('main_form', 'hero_micro'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
