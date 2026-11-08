ALTER TABLE pilot_offers
    ADD COLUMN acceptance_channel VARCHAR(32) NULL,
    ADD COLUMN acceptance_reference VARCHAR(255) NULL,
    ADD COLUMN acceptance_recorded_by VARCHAR(120) NULL,
    ADD COLUMN accepted_terms_version VARCHAR(32) NULL,
    ADD COLUMN accepted_initial_amount_mxn DECIMAL(12,2) NULL,
    ADD COLUMN accepted_renewal_amount_mxn DECIMAL(12,2) NULL;

ALTER TABLE pilot_offers
    ADD CONSTRAINT ck_pilot_offers_acceptance_evidence
    CHECK (
        status NOT IN (
            'ACCEPTED', 'PAYMENT_PENDING', 'PAID', 'ACTIVE'
        )
        OR (
            accepted_at IS NOT NULL
            AND acceptance_channel IS NOT NULL
            AND acceptance_reference IS NOT NULL
            AND acceptance_recorded_by IS NOT NULL
            AND accepted_terms_version IS NOT NULL
            AND CHAR_LENGTH(TRIM(accepted_terms_version)) > 0
            AND CHAR_LENGTH(TRIM(acceptance_reference)) > 0
            AND CHAR_LENGTH(TRIM(acceptance_recorded_by)) > 0
            AND offered_at IS NOT NULL
            AND expires_at IS NOT NULL
            AND accepted_at >= offered_at
            AND accepted_at < expires_at
            AND accepted_initial_amount_mxn IS NOT NULL
            AND accepted_renewal_amount_mxn IS NOT NULL
            AND accepted_initial_amount_mxn = initial_amount_mxn
            AND accepted_renewal_amount_mxn = renewal_amount_mxn
        )
    );
