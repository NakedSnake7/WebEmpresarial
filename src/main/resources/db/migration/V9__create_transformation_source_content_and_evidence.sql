CREATE TABLE transformation_source_contents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_document_id BIGINT NOT NULL,

    content_version INT NOT NULL,

    extraction_method VARCHAR(40) NOT NULL,
    extraction_status VARCHAR(40) NOT NULL,

    raw_text LONGTEXT NULL,

    character_count INT NULL,
    word_count INT NULL,

    detected_language_code VARCHAR(10) NULL,

    parser_name VARCHAR(120) NULL,
    parser_version VARCHAR(60) NULL,

    is_current BOOLEAN NOT NULL DEFAULT FALSE,

    started_at TIMESTAMP(6) NULL,
    extracted_at TIMESTAMP(6) NULL,
    verified_at TIMESTAMP(6) NULL,

    failure_reason VARCHAR(2000) NULL,

    created_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_source_content_document
        FOREIGN KEY (source_document_id)
        REFERENCES transformation_source_documents (id),

    CONSTRAINT uk_transformation_source_content_version
        UNIQUE (
            source_document_id,
            content_version
        ),

    CONSTRAINT chk_transformation_source_content_version
        CHECK (content_version >= 1),

    CONSTRAINT chk_transformation_source_character_count
        CHECK (
            character_count IS NULL
            OR character_count >= 0
        ),

    CONSTRAINT chk_transformation_source_word_count
        CHECK (
            word_count IS NULL
            OR word_count >= 0
        )
);

CREATE INDEX idx_transformation_source_content_status
    ON transformation_source_contents (
        source_document_id,
        extraction_status
    );

CREATE INDEX idx_transformation_source_content_current
    ON transformation_source_contents (
        source_document_id,
        is_current
    );


CREATE TABLE transformation_source_sections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_content_id BIGINT NOT NULL,

    section_code VARCHAR(80) NOT NULL,
    section_type VARCHAR(50) NOT NULL,

    title VARCHAR(500) NOT NULL,

    start_page INT NOT NULL,
    end_page INT NOT NULL,

    display_order INT NOT NULL,

    section_text LONGTEXT NOT NULL,
    summary VARCHAR(4000) NULL,

    created_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_source_section_content
        FOREIGN KEY (source_content_id)
        REFERENCES transformation_source_contents (id),

    CONSTRAINT uk_transformation_source_section_code
        UNIQUE (
            source_content_id,
            section_code
        ),

    CONSTRAINT chk_transformation_source_section_start_page
        CHECK (start_page >= 1),

    CONSTRAINT chk_transformation_source_section_end_page
        CHECK (end_page >= start_page),

    CONSTRAINT chk_transformation_source_section_order
        CHECK (display_order >= 0)
);

CREATE INDEX idx_transformation_source_section_page
    ON transformation_source_sections (
        source_content_id,
        start_page,
        end_page
    );

CREATE INDEX idx_transformation_source_section_order
    ON transformation_source_sections (
        source_content_id,
        display_order
    );


CREATE TABLE transformation_source_evidence (
    id BIGINT NOT NULL AUTO_INCREMENT,

    project_id BIGINT NOT NULL,
    source_document_id BIGINT NOT NULL,
    source_section_id BIGINT NULL,

    evidence_code VARCHAR(80) NOT NULL,

    classification VARCHAR(60) NOT NULL,
    confidence VARCHAR(40) NOT NULL,
    extraction_origin VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,

    statement VARCHAR(1000) NOT NULL,
    supporting_excerpt TEXT NOT NULL,
    interpretation VARCHAR(4000) NULL,

    page_from INT NULL,
    page_to INT NULL,

    paragraph_reference VARCHAR(200) NULL,
    element_reference VARCHAR(200) NULL,

    character_start INT NULL,
    character_end INT NULL,

    requires_human_review BOOLEAN NOT NULL DEFAULT FALSE,

    verified_by VARCHAR(180) NULL,
    rejection_reason VARCHAR(2000) NULL,

    extracted_at TIMESTAMP(6) NOT NULL,
    verified_at TIMESTAMP(6) NULL,
    rejected_at TIMESTAMP(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_transformation_evidence_project
        FOREIGN KEY (project_id)
        REFERENCES transformation_projects (id),

    CONSTRAINT fk_transformation_evidence_document
        FOREIGN KEY (source_document_id)
        REFERENCES transformation_source_documents (id),

    CONSTRAINT fk_transformation_evidence_section
        FOREIGN KEY (source_section_id)
        REFERENCES transformation_source_sections (id),

    CONSTRAINT uk_transformation_evidence_project_code
        UNIQUE (
            project_id,
            evidence_code
        ),

    CONSTRAINT chk_transformation_evidence_page_from
        CHECK (
            page_from IS NULL
            OR page_from >= 1
        ),

    CONSTRAINT chk_transformation_evidence_page_range
        CHECK (
            page_from IS NULL
            OR page_to IS NULL
            OR page_to >= page_from
        ),

    CONSTRAINT chk_transformation_evidence_character_start
        CHECK (
            character_start IS NULL
            OR character_start >= 0
        ),

    CONSTRAINT chk_transformation_evidence_character_range
        CHECK (
            character_start IS NULL
            OR character_end IS NULL
            OR character_end >= character_start
        )
);

CREATE INDEX idx_transformation_evidence_project_status
    ON transformation_source_evidence (
        project_id,
        status
    );

CREATE INDEX idx_transformation_evidence_classification
    ON transformation_source_evidence (
        project_id,
        classification
    );

CREATE INDEX idx_transformation_evidence_source
    ON transformation_source_evidence (
        source_document_id
    );

CREATE INDEX idx_transformation_evidence_page
    ON transformation_source_evidence (
        source_document_id,
        page_from,
        page_to
    );