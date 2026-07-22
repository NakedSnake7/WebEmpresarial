package com.webempresarial.store.knowledge.domain.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Representa el estado de lifecycle de un KnowledgeObject.
 *
 * <p>Las transiciones permitidas son:</p>
 *
 * <pre>
 * DRAFT
 *   -> IN_REVIEW
 *
 * IN_REVIEW
 *   -> DRAFT
 *   -> APPROVED
 *
 * APPROVED
 *   -> DRAFT
 *   -> PUBLISHED
 *
 * PUBLISHED
 *   -> ARCHIVED
 *
 * ARCHIVED
 *   -> PUBLISHED
 *   -> RETIRED
 *
 * RETIRED
 *   -> estado terminal
 * </pre>
 */
public enum KnowledgeStatus {

    DRAFT,
    IN_REVIEW,
    APPROVED,
    PUBLISHED,
    ARCHIVED,
    RETIRED;

    /**
     * Determina si el estado actual puede realizar
     * una transición hacia el estado indicado.
     */
    public boolean canTransitionTo(KnowledgeStatus target) {
        if (target == null || target == this) {
            return false;
        }

        return allowedTransitions().contains(target);
    }

    /**
     * Verifica una transición y lanza una excepción
     * cuando no está permitida.
     */
    public void validateTransitionTo(KnowledgeStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    "No se permite cambiar KnowledgeStatus de "
                            + this
                            + " a "
                            + target
            );
        }
    }

    /**
     * Devuelve las transiciones permitidas desde
     * el estado actual.
     */
    public Set<KnowledgeStatus> allowedTransitions() {
        return switch (this) {
            case DRAFT ->
                    EnumSet.of(IN_REVIEW);

            case IN_REVIEW ->
                    EnumSet.of(
                            DRAFT,
                            APPROVED
                    );

            case APPROVED ->
                    EnumSet.of(
                            DRAFT,
                            PUBLISHED
                    );

            case PUBLISHED ->
                    EnumSet.of(ARCHIVED);

            case ARCHIVED ->
                    EnumSet.of(
                            PUBLISHED,
                            RETIRED
                    );

            case RETIRED ->
                    EnumSet.noneOf(KnowledgeStatus.class);
        };
    }

    /**
     * Indica si el objeto todavía puede modificarse
     * editorialmente.
     */
    public boolean isEditable() {
        return this == DRAFT
                || this == IN_REVIEW;
    }

    /**
     * Indica si el conocimiento ha superado
     * el proceso de aprobación.
     */
    public boolean isApproved() {
        return this == APPROVED
                || this == PUBLISHED
                || this == ARCHIVED
                || this == RETIRED;
    }

    /**
     * Indica si el conocimiento está disponible
     * como versión activa publicada.
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }

    /**
     * Indica si el estado es terminal.
     */
    public boolean isTerminal() {
        return this == RETIRED;
    }

    /**
     * Estado inicial de todo KnowledgeObject.
     */
    public static KnowledgeStatus initial() {
        return DRAFT;
    }
}