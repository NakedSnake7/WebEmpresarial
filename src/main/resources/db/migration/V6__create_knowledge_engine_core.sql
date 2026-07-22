CREATE TABLE knowledge_objects (
    id BIGINT NOT NULL AUTO_INCREMENT,

    store_id BIGINT NOT NULL,

    code VARCHAR(16) NOT NULL,

    type_code VARCHAR(50) NOT NULL,
    domain VARCHAR(50) NOT NULL,
    classification VARCHAR(50) NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    context_type VARCHAR(40) NOT NULL,
    context_id VARCHAR(120) NOT NULL,

    current_version_id BIGINT NULL,

    valid_from DATETIME(6) NULL,
    valid_until DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    lock_version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_knowledge_object_store_code
        UNIQUE (store_id, code),

    INDEX idx_knowledge_object_store_status (
        store_id,
        status
    ),

    INDEX idx_knowledge_object_store_domain (
        store_id,
        domain
    ),

    INDEX idx_knowledge_object_store_context (
        store_id,
        context_type,
        context_id
    ),

    INDEX idx_knowledge_object_current_version (
        current_version_id
    ),

    CONSTRAINT fk_knowledge_object_store
        FOREIGN KEY (store_id)
        REFERENCES stores (id)
);


CREATE TABLE knowledge_object_versions (
    id BIGINT NOT NULL AUTO_INCREMENT,

    knowledge_object_id BIGINT NOT NULL,

    version_major INT NOT NULL,
    version_minor INT NOT NULL,
    version_patch INT NOT NULL,

    title VARCHAR(200) NOT NULL,
    summary VARCHAR(1000) NOT NULL,

    content LONGTEXT NOT NULL,
    content_format VARCHAR(50) NOT NULL,

    confidence DECIMAL(5,4) NOT NULL,

    source_reference VARCHAR(500) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    lock_version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_knowledge_version_object_semantic
        UNIQUE (
            knowledge_object_id,
            version_major,
            version_minor,
            version_patch
        ),

    INDEX idx_knowledge_version_object (
        knowledge_object_id
    ),

    INDEX idx_knowledge_version_created_at (
        created_at
    ),

    CONSTRAINT fk_knowledge_version_object
        FOREIGN KEY (knowledge_object_id)
        REFERENCES knowledge_objects (id)
);


ALTER TABLE knowledge_objects
    ADD CONSTRAINT fk_knowledge_object_current_version
        FOREIGN KEY (current_version_id)
        REFERENCES knowledge_object_versions (id);