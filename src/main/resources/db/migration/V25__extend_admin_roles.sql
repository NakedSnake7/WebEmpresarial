-- Roles administrativos V1 de WebEmpresarial.
--
-- Conservamos los roles existentes y agregamos:
-- STORE_OWNER -> propietario contractual de la tienda.
-- SALES       -> personal comercial / CRM.

ALTER TABLE admin_users
    MODIFY COLUMN role ENUM(
        'SUPER_ADMIN',
        'STORE_OWNER',
        'STORE_ADMIN',
        'STORE_STAFF',
        'SALES'
    ) NOT NULL;
