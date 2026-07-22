package com.webempresarial.store.knowledge.domain.enums;

/**
 * Define el impacto potencial de utilizar conocimiento
 * incorrecto, incompleto, desactualizado o no autorizado.
 */
public enum KnowledgeRiskLevel {

    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Indica si el objeto requiere revisión reforzada
     * antes de su publicación.
     */
    public boolean requiresEnhancedReview() {
        return this == HIGH
                || this == CRITICAL;
    }

    /**
     * Indica si el riesgo se considera crítico.
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Indica si el nivel actual es superior
     * al nivel proporcionado.
     */
    public boolean isHigherThan(
            KnowledgeRiskLevel other
    ) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "El nivel de riesgo comparado es obligatorio"
            );
        }

        return this.ordinal() > other.ordinal();
    }

    /**
     * Nivel predeterminado para objetos nuevos.
     */
    public static KnowledgeRiskLevel defaultValue() {
        return MEDIUM;
    }
}