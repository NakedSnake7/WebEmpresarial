package com.webempresarial.store.knowledge.api.exception;

public class KnowledgeTenantNotResolvedException
        extends RuntimeException {

    public KnowledgeTenantNotResolvedException(
            String message
    ) {
        super(message);
    }
}