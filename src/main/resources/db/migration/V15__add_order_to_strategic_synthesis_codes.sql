ALTER TABLE transformation_strategic_synthesis_evidence_codes
    ADD COLUMN position INT NOT NULL AFTER synthesis_id;

ALTER TABLE transformation_strategic_synthesis_evidence_codes
    ADD PRIMARY KEY (
        synthesis_id,
        position
    );


ALTER TABLE transformation_strategic_synthesis_artifact_codes
    ADD COLUMN position INT NOT NULL AFTER synthesis_id;

ALTER TABLE transformation_strategic_synthesis_artifact_codes
    ADD PRIMARY KEY (
        synthesis_id,
        position
    );