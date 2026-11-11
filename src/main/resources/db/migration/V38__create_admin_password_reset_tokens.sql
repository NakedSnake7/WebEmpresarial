-- D3.8.7: Recuperacion de contrasenas administrativas.
-- Los tokens originales nunca se almacenan.
-- Los hashes son SHA-256 codificados en hexadecimal.
-- Esta migracion no modifica credenciales existentes.

CREATE TABLE admin_password_reset_tokens (

    id BIGINT NOT NULL AUTO_INCREMENT,

    admin_user_id BIGINT NOT NULL,

    token_hash VARCHAR(64)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,

    created_at DATETIME(6) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    consumed_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_admin_password_reset_token_hash
        UNIQUE (token_hash),

    KEY idx_admin_password_reset_user_open (
        admin_user_id,
        consumed_at,
        expires_at
    ),

    CONSTRAINT fk_admin_password_reset_user
        FOREIGN KEY (admin_user_id)
        REFERENCES admin_users(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_admin_password_reset_expiry
        CHECK (expires_at > created_at)

) ENGINE=InnoDB;
