-- Promueve propietarios existentes al nuevo rol STORE_OWNER.
--
-- Criterio conservador:
-- 1. El usuario debe pertenecer a la tienda.
-- 2. Actualmente debe ser STORE_ADMIN.
-- 3. La tienda debe tener company_email.
-- 4. El email del administrador debe coincidir exactamente
--    (ignorando mayúsculas y espacios) con company_email.
--
-- ProvisioningService utiliza precisamente el email del
-- propietario como stores.company_email, por lo que esta
-- condición permite migrar los tenants creados mediante
-- el flujo SaaS sin promover administradores arbitrarios.

UPDATE admin_users au
JOIN stores s
    ON s.id = au.store_id
SET au.role = 'STORE_OWNER'
WHERE au.role = 'STORE_ADMIN'
  AND s.company_email IS NOT NULL
  AND TRIM(s.company_email) <> ''
  AND LOWER(TRIM(au.email))
      = LOWER(TRIM(s.company_email));
