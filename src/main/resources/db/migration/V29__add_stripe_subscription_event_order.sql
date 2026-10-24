ALTER TABLE subscriptions
    ADD COLUMN last_stripe_event_created BIGINT NULL,
    ADD COLUMN last_stripe_event_priority INT NULL,
    ADD COLUMN last_stripe_event_type VARCHAR(120) NULL;
