package com.webempresarial.store.knowledge.api.exception;

public class KnowledgeObjectNotFoundException
        extends RuntimeException {

    public KnowledgeObjectNotFoundException(
            Long knowledgeObjectId
    ) {
        super(
                "No se encontró el KnowledgeObject con id "
                        + knowledgeObjectId
        );
    }

    public KnowledgeObjectNotFoundException(
            String message
    ) {
        super(message);
    }
}