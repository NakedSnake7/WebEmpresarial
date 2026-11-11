-- D3.8.7.8
-- Vincula los tokens de recuperación con la versión
-- de credenciales existente en el momento de emisión.
-- No modificar V38, ya aplicada por Flyway.

ALTER TABLE admin_password_reset_tokens

    ADD COLUMN credential_version
        BIGINT NOT NULL DEFAULT 0
        AFTER token_hash,

    ADD CONSTRAINT ck_admin_password_reset_credential_version
        CHECK (credential_version >= 0);
