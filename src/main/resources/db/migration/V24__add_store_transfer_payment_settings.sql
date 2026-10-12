-- ============================================================
-- V24
-- Configuración de transferencias SPEI por tienda.
--
-- Las tiendas existentes empiezan con transferencia
-- deshabilitada hasta registrar datos bancarios válidos.
-- ============================================================

ALTER TABLE store_settings
    ADD COLUMN transfer_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN transfer_bank_name VARCHAR(120) NULL,
    ADD COLUMN transfer_account_holder VARCHAR(160) NULL,
    ADD COLUMN transfer_clabe VARCHAR(18) NULL;
