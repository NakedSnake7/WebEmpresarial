package com.webempresarial.store.knowledge.application.command;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.util.Objects;

/**
 * Comando de aplicación para crear un nuevo KnowledgeObject.
 *
 * <p>No recibe entidades JPA. La resolución de Store y la creación
 * de los Value Objects corresponden al caso de uso.</p>
 */
public record CreateKnowledgeObjectCommand(

        Long storeId,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeContextType contextType,

        String contextReference,

        String actor
) {

    public CreateKnowledgeObjectCommand {
        validatePositiveId(storeId);
        code = normalizeRequired(code, "El código es obligatorio");
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
    }

    private static void validatePositiveId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de Store debe ser válido"
            );
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