package com.webempresarial.store.knowledge.domain.event;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento publicado cuando un KnowledgeObject ha sido
 * creado y persistido correctamente.
 *
 * <p>El evento contiene valores simples y enums. No expone
 * entidades JPA para evitar acoplamiento y problemas de carga lazy.</p>
 */
public record KnowledgeObjectCreatedEvent(

        Long knowledgeObjectId,

        Long storeId,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        String actor,

        LocalDateTime occurredAt
) {

    public KnowledgeObjectCreatedEvent {
        validatePositiveId(
                knowledgeObjectId,
                "El identificador de KnowledgeObject debe ser válido"
        );

        validatePositiveId(
                storeId,
                "El identificador de Store debe ser válido"
        );

        code = normalizeRequired(
                code,
                "El código de conocimiento es obligatorio"
        );

        typeCode = Objects.requireNonNull(
                typeCode,
                "KnowledgeTypeCode es obligatorio"
        );

        domain = Objects.requireNonNull(
                domain,
                "KnowledgeDomain es obligatorio"
        );

        classification = Objects.requireNonNull(
                classification,
                "KnowledgeClassification es obligatoria"
        );

        riskLevel = Objects.requireNonNull(
                riskLevel,
                "KnowledgeRiskLevel es obligatorio"
        );

        status = Objects.requireNonNull(
                status,
                "KnowledgeStatus es obligatorio"
        );

        contextType = Objects.requireNonNull(
                contextType,
                "KnowledgeContextType es obligatorio"
        );

        contextReference = normalizeRequired(
                contextReference,
                "La referencia del contexto es obligatoria"
        );

        actor = normalizeRequired(
                actor,
                "El actor es obligatorio"
        );

        occurredAt = Objects.requireNonNull(
                occurredAt,
                "La fecha del evento es obligatoria"
        );
    }

    private static void validatePositiveId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String normalizeRequired(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}