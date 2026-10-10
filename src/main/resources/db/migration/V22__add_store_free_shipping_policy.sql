-- ============================================================
-- V22
-- Política de envío gratis por tienda.
--
-- Mantiene el comportamiento histórico:
--   envío gratis habilitado
--   a partir de $1,250.00
-- ============================================================

ALTER TABLE store_settings
    ADD COLUMN free_shipping_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN free_shipping_threshold DECIMAL(12,2) NOT NULL DEFAULT 1250.00;
