-- ============================================================
-- V21
-- orders.user_id debe referenciar clientes(id), no users(id).
--
-- Producción heredó dos FKs sobre orders.user_id:
--   1. orders.user_id -> users.id      (incorrecta)
--   2. orders.user_id -> clientes.id   (correcta)
--
-- La migración elimina únicamente la FK incorrecta si existe.
-- También garantiza que exista la relación correcta hacia clientes.
-- ============================================================

SET @wrong_fk = (
    SELECT kcu.CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = DATABASE()
      AND kcu.TABLE_NAME = 'orders'
      AND kcu.COLUMN_NAME = 'user_id'
      AND kcu.REFERENCED_TABLE_NAME = 'users'
      AND kcu.REFERENCED_COLUMN_NAME = 'id'
    LIMIT 1
);

SET @drop_wrong_fk_sql = IF(
    @wrong_fk IS NULL,
    'SELECT 1',
    CONCAT(
        'ALTER TABLE `orders` DROP FOREIGN KEY `',
        REPLACE(@wrong_fk, '`', '``'),
        '`'
    )
);

PREPARE drop_wrong_fk_stmt FROM @drop_wrong_fk_sql;
EXECUTE drop_wrong_fk_stmt;
DEALLOCATE PREPARE drop_wrong_fk_stmt;


SET @correct_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = DATABASE()
      AND kcu.TABLE_NAME = 'orders'
      AND kcu.COLUMN_NAME = 'user_id'
      AND kcu.REFERENCED_TABLE_NAME = 'clientes'
      AND kcu.REFERENCED_COLUMN_NAME = 'id'
);

SET @add_correct_fk_sql = IF(
    @correct_fk_exists > 0,
    'SELECT 1',
    'ALTER TABLE `orders`
       ADD CONSTRAINT `fk_orders_user_cliente`
       FOREIGN KEY (`user_id`)
       REFERENCES `clientes` (`id`)'
);

PREPARE add_correct_fk_stmt FROM @add_correct_fk_sql;
EXECUTE add_correct_fk_stmt;
DEALLOCATE PREPARE add_correct_fk_stmt;
