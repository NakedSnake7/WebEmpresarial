package com.webempresarial.store.knowledge.domain.enums;

/**
 * Define el nivel de acceso y sensibilidad
 * de un objeto de conocimiento.
 */
public enum KnowledgeClassification {

    PUBLIC,
    INTERNAL,
    CONFIDENTIAL,
    RESTRICTED;

    /**
     * Indica si el conocimiento puede exponerse públicamente.
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }

    /**
     * Indica si el conocimiento requiere controles
     * especiales de acceso.
     */
    public boolean requiresRestrictedAccess() {
        return this == CONFIDENTIAL
                || this == RESTRICTED;
    }

    /**
     * Indica si el nivel actual es más restrictivo
     * que el nivel proporcionado.
     */
    public boolean isMoreRestrictiveThan(
            KnowledgeClassification other
    ) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "La clasificación comparada es obligatoria"
            );
        }

        return this.ordinal() > other.ordinal();
    }

    /**
     * Clasificación predeterminada para objetos nuevos.
     */
    public static KnowledgeClassification defaultValue() {
        return INTERNAL;
    }
}