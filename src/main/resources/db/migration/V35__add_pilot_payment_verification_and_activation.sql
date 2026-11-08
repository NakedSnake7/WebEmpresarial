-- Evidencia de conciliación y vigencia del piloto.
-- La referencia debe identificar un movimiento bancario único.

ALTER TABLE pilot_offers
    ADD COLUMN payment_evidence_reference VARCHAR(255) NULL,
    ADD COLUMN payment_verified_by VARCHAR(120) NULL,
    ADD COLUMN payment_verified_at DATETIME(6) NULL,
    ADD COLUMN pilot_started_at DATETIME(6) NULL,
    ADD COLUMN pilot_ends_at DATETIME(6) NULL;

CREATE UNIQUE INDEX uk_pilot_offers_payment_evidence
    ON pilot_offers (payment_evidence_reference);

ALTER TABLE pilot_offers
    ADD CONSTRAINT ck_pilot_offers_pending_method
    CHECK (
        status NOT IN ('PAYMENT_PENDING', 'PAID', 'ACTIVE')
        OR (
            payment_method IS NOT NULL
            AND payment_method IN ('TRANSFER', 'STRIPE')
        )
    );

ALTER TABLE pilot_offers
    ADD CONSTRAINT ck_pilot_offers_paid_evidence
    CHECK (
        status NOT IN ('PAID', 'ACTIVE')
        OR (
            paid_at IS NOT NULL
            AND payment_method IS NOT NULL
            AND (
                (
                    payment_method = 'TRANSFER'
                    AND payment_evidence_reference IS NOT NULL
                    AND payment_verified_by IS NOT NULL
                    AND payment_verified_at IS NOT NULL
                    AND CHAR_LENGTH(
                        TRIM(payment_evidence_reference)
                    ) > 0
                    AND CHAR_LENGTH(
                        TRIM(payment_verified_by)
                    ) > 0
                    AND payment_verified_at >= accepted_at
                )
                OR (
                    payment_method = 'STRIPE'
                    AND stripe_payment_intent_id IS NOT NULL
                    AND CHAR_LENGTH(
                        TRIM(stripe_payment_intent_id)
                    ) > 0
                )
            )
        )
    );

ALTER TABLE pilot_offers
    ADD CONSTRAINT ck_pilot_offers_activation_period
    CHECK (
        status <> 'ACTIVE'
        OR (
            pilot_started_at IS NOT NULL
            AND pilot_ends_at IS NOT NULL
            AND paid_at IS NOT NULL
            AND pilot_started_at >= paid_at
            AND pilot_ends_at = DATE_ADD(
                pilot_started_at,
                INTERVAL 30 DAY
            )
        )
    );
