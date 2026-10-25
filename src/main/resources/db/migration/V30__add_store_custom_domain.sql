ALTER TABLE stores
    ADD COLUMN custom_domain VARCHAR(253) NULL AFTER dominio,
    ADD COLUMN custom_domain_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER custom_domain,
    ADD CONSTRAINT uk_store_custom_domain UNIQUE (custom_domain);
