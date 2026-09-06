CREATE TABLE transformation_strategic_syntheses (

    id BIGINT NOT NULL AUTO_INCREMENT,

    project_id BIGINT NOT NULL,

    finding_statement TEXT NOT NULL,

    business_problem_statement TEXT NOT NULL,

    business_objective_statement TEXT NOT NULL,

    strategic_opportunity_statement TEXT NOT NULL,

    strategic_thesis TEXT NOT NULL,

    coverage_status VARCHAR(40) NOT NULL,

    coverage_percentage INT NOT NULL,

    maximum_trace_depth INT NOT NULL,

    confidence VARCHAR(30) NOT NULL,

    origin VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_strategic_synthesis_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id)
);

CREATE INDEX idx_strategic_synthesis_project
    ON transformation_strategic_syntheses (
        project_id,
        created_at
    );

CREATE INDEX idx_strategic_synthesis_project_origin
    ON transformation_strategic_syntheses (
        project_id,
        origin,
        created_at
    );

CREATE INDEX idx_strategic_synthesis_project_status
    ON transformation_strategic_syntheses (
        project_id,
        status
    );


CREATE TABLE transformation_strategic_synthesis_evidence_codes (

    synthesis_id BIGINT NOT NULL,

    evidence_code VARCHAR(180) NOT NULL,

    CONSTRAINT fk_strategic_synthesis_evidence
        FOREIGN KEY (synthesis_id)
        REFERENCES transformation_strategic_syntheses (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_strategic_synthesis_evidence_synthesis
    ON transformation_strategic_synthesis_evidence_codes (
        synthesis_id
    );


CREATE TABLE transformation_strategic_synthesis_artifact_codes (

    synthesis_id BIGINT NOT NULL,

    artifact_code VARCHAR(180) NOT NULL,

    CONSTRAINT fk_strategic_synthesis_artifacts
        FOREIGN KEY (synthesis_id)
        REFERENCES transformation_strategic_syntheses (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_strategic_synthesis_artifact_synthesis
    ON transformation_strategic_synthesis_artifact_codes (
        synthesis_id
    );