package com.webempresarial.store.knowledge.api.exception;

public class KnowledgeVersionNotFoundException
        extends RuntimeException {

    public KnowledgeVersionNotFoundException(
            Long versionId
    ) {
        super(
                "No se encontró la KnowledgeObjectVersion con id "
                        + versionId
        );
    }
}