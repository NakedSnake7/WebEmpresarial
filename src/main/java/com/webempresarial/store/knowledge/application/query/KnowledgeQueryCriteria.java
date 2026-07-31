package com.webempresarial.store.knowledge.application.query;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Criterios desacoplados de persistencia para consultar
 * KnowledgeObjects dentro de una Store.
 *
 * <p>Todos los filtros, excepto storeId, son opcionales.</p>
 */


public record KnowledgeQueryCriteria(

        Long storeId,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        BigDecimal minimumConfidence,

        LocalDateTime effectiveAt,

        String text,

        int page,

        int size
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final int MAX_TEXT_LENGTH = 250;

    public KnowledgeQueryCriteria {
        validateStoreId(storeId);

        code = normalizeOptional(code);
        contextReference = normalizeOptional(contextReference);
        text = normalizeOptional(text);

        validateContext(
                contextType,
                contextReference
        );

        minimumConfidence =
                validateConfidence(minimumConfidence);

        if (text != null
                && text.length() > MAX_TEXT_LENGTH) {

            throw new IllegalArgumentException(
                    "El texto de búsqueda no puede superar "
                            + MAX_TEXT_LENGTH
                            + " caracteres"
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException(
                    "El número de página no puede ser negativo"
            );
        }

        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y "
                            + MAX_SIZE
            );
        }
    }

    /**
     * Crea criterios sin filtros adicionales.
     */
    public static KnowledgeQueryCriteria forStore(
            Long storeId
    ) {
        return builder(storeId).build();
    }

    /**
     * Inicia un builder con paginación predeterminada.
     */
    public static Builder builder(Long storeId) {
        return new Builder(storeId);
    }

    public boolean hasCode() {
        return code != null;
    }

    public boolean hasContext() {
        return contextType != null;
    }

    public boolean hasMinimumConfidence() {
        return minimumConfidence != null;
    }

    public boolean hasEffectiveMoment() {
        return effectiveAt != null;
    }

    public boolean hasText() {
        return text != null;
    }

    public boolean requiresVersionJoin() {
        return hasMinimumConfidence()
                || hasEffectiveMoment()
                || hasText();
    }

    private static void validateStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de Store debe ser válido"
            );
        }
    }

    private static void validateContext(
            KnowledgeContextType contextType,
            String contextReference
    ) {
        if (contextType == null
                && contextReference != null) {

            throw new IllegalArgumentException(
                    "No puede indicarse una referencia de contexto "
                            + "sin KnowledgeContextType"
            );
        }

        if (contextType != null
                && contextReference == null) {

            throw new IllegalArgumentException(
                    "La referencia del contexto es obligatoria "
                            + "cuando se indica KnowledgeContextType"
            );
        }
    }

    private static BigDecimal validateConfidence(
            BigDecimal value
    ) {
        if (value == null) {
            return null;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "La confianza mínima debe estar entre 0 y 1"
            );
        }

        return value;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public static final class Builder {

        private final Long storeId;

        private String code;
        private KnowledgeTypeCode typeCode;
        private KnowledgeDomain domain;
        private KnowledgeClassification classification;
        private KnowledgeRiskLevel riskLevel;
        private KnowledgeStatus status;
        private KnowledgeContextType contextType;
        private String contextReference;
        private BigDecimal minimumConfidence;
        private LocalDateTime effectiveAt;
        private String text;

        private int page = DEFAULT_PAGE;
        private int size = DEFAULT_SIZE;

        private Builder(Long storeId) {
            validateStoreId(storeId);
            this.storeId = storeId;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder typeCode(
                KnowledgeTypeCode typeCode
        ) {
            this.typeCode = typeCode;
            return this;
        }

        public Builder domain(
                KnowledgeDomain domain
        ) {
            this.domain = domain;
            return this;
        }

        public Builder classification(
                KnowledgeClassification classification
        ) {
            this.classification = classification;
            return this;
        }

        public Builder riskLevel(
                KnowledgeRiskLevel riskLevel
        ) {
            this.riskLevel = riskLevel;
            return this;
        }

        public Builder status(
                KnowledgeStatus status
        ) {
            this.status = status;
            return this;
        }

        public Builder publishedOnly() {
            this.status = KnowledgeStatus.PUBLISHED;
            return this;
        }

        public Builder context(
                KnowledgeContextType contextType,
                String contextReference
        ) {
            this.contextType = contextType;
            this.contextReference = contextReference;
            return this;
        }

        public Builder minimumConfidence(
                BigDecimal minimumConfidence
        ) {
            this.minimumConfidence = minimumConfidence;
            return this;
        }

        public Builder effectiveAt(
                LocalDateTime effectiveAt
        ) {
            this.effectiveAt = effectiveAt;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public KnowledgeQueryCriteria build() {
            return new KnowledgeQueryCriteria(
                    storeId,
                    code,
                    typeCode,
                    domain,
                    classification,
                    riskLevel,
                    status,
                    contextType,
                    contextReference,
                    minimumConfidence,
                    effectiveAt,
                    text,
                    page,
                    size
            );
        }
    }
}