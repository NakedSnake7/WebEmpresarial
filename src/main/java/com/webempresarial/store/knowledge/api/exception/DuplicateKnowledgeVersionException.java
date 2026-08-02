package com.webempresarial.store.knowledge.api.exception;

public class DuplicateKnowledgeVersionException
        extends RuntimeException {

    public DuplicateKnowledgeVersionException(
            String semanticVersion
    ) {
        super(
                "La versión "
                        + semanticVersion
                        + " ya existe para el KnowledgeObject"
        );
    }
}