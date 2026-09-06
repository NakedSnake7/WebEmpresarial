CREATE TABLE transformation_strategic_relationships (

    id BIGINT NOT NULL AUTO_INCREMENT,

    project_id BIGINT NOT NULL,

    source_artifact_id BIGINT NOT NULL,

    target_artifact_id BIGINT NOT NULL,

    relationship_type VARCHAR(40) NOT NULL,

    status VARCHAR(30) NOT NULL,

    origin VARCHAR(30) NOT NULL,

    rationale VARCHAR(2000) NULL,

    created_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_strategic_relationship_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT fk_strategic_relationship_source
        FOREIGN KEY (source_artifact_id)
        REFERENCES transformation_strategic_artifacts (id),

    CONSTRAINT fk_strategic_relationship_target
        FOREIGN KEY (target_artifact_id)
        REFERENCES transformation_strategic_artifacts (id),

    CONSTRAINT uk_strategic_relationship
        UNIQUE (
            project_id,
            source_artifact_id,
            target_artifact_id,
            relationship_type
        )
);

CREATE INDEX idx_strategic_relationship_project
    ON transformation_strategic_relationships (
        project_id
    );

CREATE INDEX idx_strategic_relationship_source
    ON transformation_strategic_relationships (
        source_artifact_id
    );

CREATE INDEX idx_strategic_relationship_target
    ON transformation_strategic_relationships (
        target_artifact_id
    );