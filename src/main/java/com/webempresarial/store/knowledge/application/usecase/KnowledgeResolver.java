package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.result.KnowledgeSnapshot;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Puerto de entrada para resolver conocimiento publicado.
 *
 * <p>Todos los métodos incluyen storeId para preservar el
 * aislamiento multi-tenant.</p>
 */
public interface KnowledgeResolver {

    /**
     * Resuelve el conocimiento vigente en el momento actual.
     */
    Optional<KnowledgeSnapshot> resolve(
            Long storeId,
            String code
    );

    /**
     * Resuelve el conocimiento aplicable en un momento específico.
     */
    Optional<KnowledgeSnapshot> resolveAt(
            Long storeId,
            String code,
            LocalDateTime moment
    );

    /**
     * Resuelve el conocimiento vigente o lanza una excepción
     * cuando no se encuentra una publicación aplicable.
     */
    KnowledgeSnapshot require(
            Long storeId,
            String code
    );

    /**
     * Resuelve el conocimiento aplicable en el momento indicado
     * o lanza una excepción.
     */
    KnowledgeSnapshot requireAt(
            Long storeId,
            String code,
            LocalDateTime moment
    );
}