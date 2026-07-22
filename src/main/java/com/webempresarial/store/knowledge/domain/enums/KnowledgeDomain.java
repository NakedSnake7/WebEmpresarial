package com.webempresarial.store.knowledge.domain.enums;

/**
 * Define el dominio funcional principal al que pertenece
 * un objeto de conocimiento.
 */
public enum KnowledgeDomain {

    STRATEGY,
    ARCHITECTURE,
    PRODUCT,
    ENGINEERING,
    SECURITY,
    DATA,
    OPERATIONS,
    SALES,
    MARKETING,
    FINANCE,
    LEGAL,
    GOVERNANCE,
    CUSTOMER_SUCCESS,
    HUMAN_RESOURCES,
    CONSULTING,
    PLATFORM,
    COMMERCE,
    CRM,
    AUTOMATION,
    ARTIFICIAL_INTELLIGENCE;

    /**
     * Indica si el dominio pertenece principalmente
     * al núcleo tecnológico de la plataforma.
     */
    public boolean isTechnologyDomain() {
        return this == ARCHITECTURE
                || this == ENGINEERING
                || this == SECURITY
                || this == DATA
                || this == PLATFORM
                || this == AUTOMATION
                || this == ARTIFICIAL_INTELLIGENCE;
    }

    /**
     * Indica si el dominio está orientado directamente
     * al crecimiento comercial.
     */
    public boolean isCommercialDomain() {
        return this == SALES
                || this == MARKETING
                || this == CUSTOMER_SUCCESS
                || this == COMMERCE
                || this == CRM;
    }

    /**
     * Indica si el dominio está relacionado con gobierno
     * y control organizacional.
     */
    public boolean isGovernanceDomain() {
        return this == GOVERNANCE
                || this == LEGAL
                || this == FINANCE
                || this == SECURITY;
    }
}