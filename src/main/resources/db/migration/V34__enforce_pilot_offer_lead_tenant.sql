-- Asegura que el lead pertenezca a la tienda de la oferta.
-- Se añade una clave candidata compuesta en leads.

ALTER TABLE leads
    ADD CONSTRAINT uk_leads_id_store
    UNIQUE (id, store_id);

-- Ambas columnas son NOT NULL en pilot_offers.
-- Se conserva la FK individual existente por compatibilidad.

ALTER TABLE pilot_offers
    ADD CONSTRAINT fk_pilot_offers_lead_store
    FOREIGN KEY (lead_id, store_id)
    REFERENCES leads (id, store_id);
