-- ============================================================
-- V23
-- Costo normal de envío configurable por tienda.
--
-- Mantiene el comportamiento histórico:
--   costo de envío = $120.00
-- ============================================================

ALTER TABLE store_settings
    ADD COLUMN shipping_cost DECIMAL(12,2)
    NOT NULL DEFAULT 120.00;
