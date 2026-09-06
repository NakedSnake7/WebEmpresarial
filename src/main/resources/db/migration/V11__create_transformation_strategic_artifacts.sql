CREATE TABLE transformation_strategic_artifacts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,

    artifact_code VARCHAR(80) NOT NULL,
    artifact_type VARCHAR(60) NOT NULL,

    status VARCHAR(40) NOT NULL,
    confidence VARCHAR(40) NOT NULL,
    origin VARCHAR(40) NOT NULL,
    priority VARCHAR(30) NOT NULL,

    statement VARCHAR(1000) NOT NULL,
    rationale VARCHAR(4000) NULL,
    business_implication VARCHAR(4000) NULL,

    requires_review BOOLEAN NOT NULL DEFAULT FALSE,

    verified_by VARCHAR(180) NULL,
    rejection_reason VARCHAR(2000) NULL,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    verified_at TIMESTAMP(6) NULL,
    rejected_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_strategic_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT uk_transformation_strategic_project_code
        UNIQUE (
            project_id,
            artifact_code
        )
);

CREATE INDEX idx_transformation_strategic_project_type
    ON transformation_strategic_artifacts (
        project_id,
        artifact_type
    );

CREATE INDEX idx_transformation_strategic_project_status
    ON transformation_strategic_artifacts (
        project_id,
        status
    );

CREATE INDEX idx_transformation_strategic_priority
    ON transformation_strategic_artifacts (
        project_id,
        priority
    );