package com.webempresarial.store.knowledge.application.exception;

/**
 * Excepción lanzada cuando no puede resolverse conocimiento
 * publicado y vigente para una Store.
 */
public class KnowledgeResolutionException
        extends RuntimeException {

    public KnowledgeResolutionException(String message) {
        super(message);
    }

    public KnowledgeResolutionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}