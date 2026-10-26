/*
 * Customer authentication identity is tenant-scoped.
 *
 * clientes already enforces:
 *
 *     UNIQUE (email, store_id)
 *
 * auth_users therefore must not enforce global email uniqueness.
 * The one-to-one cliente_id constraint remains unchanged.
 */

ALTER TABLE auth_users
    DROP INDEX UK6jqfsuvys3lan090p4mk16a5t;

CREATE INDEX idx_auth_users_email
    ON auth_users (email);
