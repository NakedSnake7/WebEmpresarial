CREATE TABLE transformation_projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(180) NOT NULL,

    client_name VARCHAR(180) NOT NULL,
    client_website VARCHAR(500) NULL,

    project_type VARCHAR(60) NOT NULL,
    status VARCHAR(60) NOT NULL,

    executive_intent TEXT NULL,

    source_of_truth_locked BOOLEAN NOT NULL DEFAULT FALSE,
    current_blueprint_version INT NULL,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_project_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id),

    CONSTRAINT uk_transformation_project_store_code
        UNIQUE (store_id, code)
);

CREATE INDEX idx_transformation_project_store_status
    ON transformation_projects (store_id, status);

CREATE INDEX idx_transformation_project_created_at
    ON transformation_projects (created_at);


CREATE TABLE transformation_source_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,

    source_type VARCHAR(80) NOT NULL,
    source_role VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,

    original_filename VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    storage_reference VARCHAR(1000) NOT NULL,

    checksum_sha256 VARCHAR(64) NOT NULL,
    document_version INT NOT NULL,
    language_code VARCHAR(10) NOT NULL,

    is_authoritative BOOLEAN NOT NULL DEFAULT FALSE,
    page_count INT NULL,

    registered_at TIMESTAMP(6) NOT NULL,
    parsed_at TIMESTAMP(6) NULL,
    analyzed_at TIMESTAMP(6) NULL,
    verified_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_source_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT uk_transformation_source_project_type_version
        UNIQUE (
            project_id,
            source_type,
            document_version
        ),

    CONSTRAINT uk_transformation_source_project_checksum
        UNIQUE (
            project_id,
            checksum_sha256
        ),

    CONSTRAINT chk_transformation_source_version
        CHECK (document_version >= 1),

    CONSTRAINT chk_transformation_source_page_count
        CHECK (
            page_count IS NULL
            OR page_count >= 1
        )
);

CREATE INDEX idx_transformation_source_project_status
    ON transformation_source_documents (project_id, status);

CREATE INDEX idx_transformation_source_type
    ON transformation_source_documents (source_type);