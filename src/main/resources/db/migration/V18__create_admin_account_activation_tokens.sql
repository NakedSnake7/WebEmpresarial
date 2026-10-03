CREATE TABLE admin_account_activation_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used BIT(1) NOT NULL DEFAULT b'0',

    PRIMARY KEY (id),

    CONSTRAINT uk_admin_account_activation_token
        UNIQUE (token),

    KEY idx_admin_activation_admin_user (
        admin_user_id
    ),

    KEY idx_admin_activation_expires_at (
        expires_at
    ),

    CONSTRAINT fk_admin_activation_admin_user
        FOREIGN KEY (admin_user_id)
        REFERENCES admin_users(id)
        ON DELETE CASCADE
);