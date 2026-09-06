ALTER TABLE transformation_strategic_artifacts
    ADD COLUMN source_evidence_id BIGINT NULL;

ALTER TABLE transformation_strategic_artifacts
    ADD CONSTRAINT fk_strategic_artifact_source_evidence
        FOREIGN KEY (source_evidence_id)
        REFERENCES transformation_source_evidence (id);

ALTER TABLE transformation_strategic_artifacts
    ADD CONSTRAINT uk_strategic_evidence_type
        UNIQUE (
            source_evidence_id,
            artifact_type
        );

CREATE INDEX idx_strategic_source_evidence
    ON transformation_strategic_artifacts (
        source_evidence_id
    );