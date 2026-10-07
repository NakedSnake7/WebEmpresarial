-- Align feature_usage.feature with the current Feature enum.
-- INVENTORY_KARDEX exists in the application model but was never
-- incorporated into the Flyway migration chain.

ALTER TABLE feature_usage
    MODIFY COLUMN feature ENUM(
        'ANALYTICS',
        'API_ACCESS',
        'AUTOMATIONS',
        'CATEGORIES',
        'CHECKOUT',
        'COUPONS',
        'CRM',
        'CUSTOM_DOMAIN',
        'EMAIL_MARKETING',
        'INVENTORY',
        'INVENTORY_KARDEX',
        'LEADS',
        'MULTI_USER',
        'ORDERS',
        'PIPELINE',
        'PRODUCTS',
        'PROPOSALS',
        'REVIEWS',
        'STRIPE_CONNECT',
        'TASKS',
        'WHATSAPP_AUTOMATION',
        'WHITE_LABEL_FULL'
    ) NOT NULL;
