package com.webempresarial.store.knowledge.domain.enums;

/**
 * Define el tipo de contexto funcional al que pertenece
 * o sobre el que aplica un objeto de conocimiento.
 *
 * <p>El contexto no reemplaza a Store. Store continúa siendo
 * la frontera obligatoria de aislamiento multi-tenant.</p>
 */
public enum KnowledgeContextType {

    PLATFORM,
    STORE,
    CLIENT,
    PROJECT,
    PRODUCT,
    SERVICE,
    SYSTEM,
    PROCESS
}