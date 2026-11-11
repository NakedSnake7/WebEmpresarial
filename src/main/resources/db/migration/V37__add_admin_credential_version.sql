-- D3.8.6 — Versionado de credenciales administrativas.
-- Versión inicial de las cuentas existentes: 0.
-- No modifica contraseñas, roles, tenants ni estados.

ALTER TABLE admin_users
    ADD COLUMN credential_version BIGINT NOT NULL DEFAULT 0;
