ALTER TABLE subscriptions
    ADD CONSTRAINT uk_subscription_stripe_subscription_id
    UNIQUE (stripe_subscription_id);
