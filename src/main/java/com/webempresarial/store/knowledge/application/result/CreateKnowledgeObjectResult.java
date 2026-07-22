package com.webempresarial.store.knowledge.application.result;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;

import java.time.LocalDateTime;

/**
 * Resultado producido después de crear un KnowledgeObject.
 *
 * <p>Este contrato evita exponer directamente la entidad JPA
 * desde la capa de aplicación.</p>
 */
public record CreateKnowledgeObjectResult(

        Long id,

        Long storeId,

        String code,

        KnowledgeTypeCode typeCode,

        KnowledgeDomain domain,

        KnowledgeClassification classification,

        KnowledgeRiskLevel riskLevel,

        KnowledgeStatus status,

        KnowledgeContextType contextType,

        String contextReference,

        String createdBy,

        LocalDateTime createdAt,

        Long lockVersion
) {

    public CreateKnowledgeObjectResult {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de KnowledgeObject debe ser válido"
            );
        }

        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de Store debe ser válido"
            );
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "El código es obligatorio"
            );
        }

        if (typeCode == null) {
            throw new IllegalArgumentException(
                    "KnowledgeTypeCode es obligatorio"
            );
        }

        if (domain == null) {
            throw new IllegalArgumentException(
                    "KnowledgeDomain es obligatorio"
            );
        }

        if (classification == null) {
            throw new IllegalArgumentException(
                    "KnowledgeClassification es obligatoria"
            );
        }

        if (riskLevel == null) {
            throw new IllegalArgumentException(
                    "KnowledgeRiskLevel es obligatorio"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "KnowledgeStatus es obligatorio"
            );
        }

        if (contextType == null) {
            throw new IllegalArgumentException(
                    "KnowledgeContextType es obligatorio"
            );
        }

        if (contextReference == null
                || contextReference.isBlank()) {

            throw new IllegalArgumentException(
                    "La referencia del contexto es obligatoria"
            );
        }

        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException(
                    "El actor creador es obligatorio"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación es obligatoria"
            );
        }

        if (lockVersion == null || lockVersion < 0) {
            throw new IllegalArgumentException(
                    "La versión de bloqueo debe ser válida"
            );
        }

        code = code.trim();
        contextReference = contextReference.trim();
        createdBy = createdBy.trim();
    }
}