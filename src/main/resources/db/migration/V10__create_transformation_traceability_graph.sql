CREATE TABLE transformation_traceability_nodes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,

    node_code VARCHAR(100) NOT NULL,
    node_type VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL,
    origin VARCHAR(40) NOT NULL,

    title VARCHAR(500) NOT NULL,
    description VARCHAR(4000) NULL,

    external_reference VARCHAR(255) NOT NULL,
    external_entity_type VARCHAR(180) NOT NULL,

    requires_review BOOLEAN NOT NULL DEFAULT FALSE,

    verified_by VARCHAR(180) NULL,
    rejection_reason VARCHAR(2000) NULL,

    created_at TIMESTAMP(6) NOT NULL,
    verified_at TIMESTAMP(6) NULL,
    rejected_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_trace_node_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT uk_transformation_trace_node_project_code
        UNIQUE (
            project_id,
            node_code
        ),

    CONSTRAINT uk_transformation_trace_node_external_ref
        UNIQUE (
            project_id,
            node_type,
            external_reference
        )
);

CREATE INDEX idx_transformation_trace_node_project_type
    ON transformation_traceability_nodes (
        project_id,
        node_type
    );

CREATE INDEX idx_transformation_trace_node_project_status
    ON transformation_traceability_nodes (
        project_id,
        status
    );

CREATE INDEX idx_transformation_trace_node_external_ref
    ON transformation_traceability_nodes (
        external_reference
    );


CREATE TABLE transformation_traceability_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,

    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,

    relation_type VARCHAR(50) NOT NULL,
    strength VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    origin VARCHAR(40) NOT NULL,

    rationale VARCHAR(4000) NULL,

    requires_review BOOLEAN NOT NULL DEFAULT FALSE,

    verified_by VARCHAR(180) NULL,
    rejection_reason VARCHAR(2000) NULL,

    created_at TIMESTAMP(6) NOT NULL,
    verified_at TIMESTAMP(6) NULL,
    rejected_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_trace_link_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT fk_transformation_trace_link_source
        FOREIGN KEY (source_node_id)
        REFERENCES transformation_traceability_nodes (id),

    CONSTRAINT fk_transformation_trace_link_target
        FOREIGN KEY (target_node_id)
        REFERENCES transformation_traceability_nodes (id),

    CONSTRAINT uk_transformation_trace_link
        UNIQUE (
            project_id,
            source_node_id,
            target_node_id,
            relation_type
        ),

    CONSTRAINT chk_transformation_trace_distinct_nodes
        CHECK (source_node_id <> target_node_id)
);

CREATE INDEX idx_transformation_trace_link_source
    ON transformation_traceability_links (
        source_node_id,
        status
    );

CREATE INDEX idx_transformation_trace_link_target
    ON transformation_traceability_links (
        target_node_id,
        status
    );

CREATE INDEX idx_transformation_trace_link_relation
    ON transformation_traceability_links (
        project_id,
        relation_type
    );


CREATE TABLE transformation_provenance_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,

    traceability_node_id BIGINT NULL,
    traceability_link_id BIGINT NULL,

    action VARCHAR(40) NOT NULL,
    origin VARCHAR(40) NOT NULL,

    actor VARCHAR(180) NOT NULL,
    actor_type VARCHAR(60) NOT NULL,

    process_reference VARCHAR(255) NULL,
    explanation VARCHAR(4000) NULL,

    recorded_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_provenance_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT fk_transformation_provenance_node
        FOREIGN KEY (traceability_node_id)
        REFERENCES transformation_traceability_nodes (id),

    CONSTRAINT fk_transformation_provenance_link
        FOREIGN KEY (traceability_link_id)
        REFERENCES transformation_traceability_links (id),

    CONSTRAINT chk_transformation_provenance_target
        CHECK (
            (
                traceability_node_id IS NOT NULL
                AND traceability_link_id IS NULL
            )
            OR
            (
                traceability_node_id IS NULL
                AND traceability_link_id IS NOT NULL
            )
        )
);

CREATE INDEX idx_transformation_provenance_project
    ON transformation_provenance_records (
        project_id,
        recorded_at
    );

CREATE INDEX idx_transformation_provenance_node
    ON transformation_provenance_records (
        traceability_node_id,
        recorded_at
    );

CREATE INDEX idx_transformation_provenance_actor
    ON transformation_provenance_records (
        actor
    );