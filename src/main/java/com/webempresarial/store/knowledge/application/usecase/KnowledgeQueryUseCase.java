package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.query.KnowledgeQueryCriteria;
import com.webempresarial.store.knowledge.application.result.KnowledgeQueryPage;

/**
 * Puerto de entrada para búsquedas avanzadas de conocimiento.
 */
public interface KnowledgeQueryUseCase {

    KnowledgeQueryPage search(
            KnowledgeQueryCriteria criteria
    );
}