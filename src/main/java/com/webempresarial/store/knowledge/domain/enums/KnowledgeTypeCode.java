package com.webempresarial.store.knowledge.domain.enums;

/**
 * Define la naturaleza estructural de un KnowledgeObject.
 */
public enum KnowledgeTypeCode {

    ARCHITECTURE,
    POLICY,
    PROCEDURE,
    STANDARD,
    GUIDELINE,
    PLAYBOOK,
    FRAMEWORK,
    BLUEPRINT,
    SPECIFICATION,
    DECISION,
    PRINCIPLE,
    REFERENCE,
    TEMPLATE;

    /**
     * Indica si el tipo representa una definición normativa
     * o de cumplimiento esperado.
     */
    public boolean isGovernanceType() {
        return this == POLICY
                || this == STANDARD
                || this == PRINCIPLE
                || this == DECISION;
    }

    /**
     * Indica si el tipo está orientado a ejecución práctica.
     */
    public boolean isOperationalType() {
        return this == PROCEDURE
                || this == GUIDELINE
                || this == PLAYBOOK
                || this == TEMPLATE;
    }

    /**
     * Indica si el tipo describe diseño o estructura.
     */
    public boolean isStructuralType() {
        return this == ARCHITECTURE
                || this == FRAMEWORK
                || this == BLUEPRINT
                || this == SPECIFICATION;
    }
}