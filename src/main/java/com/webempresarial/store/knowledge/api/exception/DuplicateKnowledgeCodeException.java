package com.webempresarial.store.knowledge.api.exception;

public class DuplicateKnowledgeCodeException
        extends RuntimeException {

    public DuplicateKnowledgeCodeException(
            String code
    ) {
        super(
                "Ya existe un KnowledgeObject con el código "
                        + code
                        + " dentro de la tienda actual"
        );
    }
}