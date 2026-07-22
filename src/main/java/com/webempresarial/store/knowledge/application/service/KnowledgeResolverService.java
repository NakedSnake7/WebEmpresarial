package com.webempresarial.store.knowledge.application.service;

import com.webempresarial.store.knowledge.application.exception.KnowledgeResolutionException;
import com.webempresarial.store.knowledge.application.result.KnowledgeSnapshot;
import com.webempresarial.store.knowledge.application.usecase.KnowledgeResolver;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObject;
import com.webempresarial.store.knowledge.domain.model.KnowledgeObjectVersion;
import com.webempresarial.store.knowledge.domain.value.KnowledgeCode;
import com.webempresarial.store.knowledge.infrastructure.repository.KnowledgeObjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementación del resolver de conocimiento publicado.
 */
@Service
public class KnowledgeResolverService
        implements KnowledgeResolver {

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final Clock clock;

    @Autowired
    public KnowledgeResolverService(
            KnowledgeObjectRepository knowledgeObjectRepository
    ) {
        this(
                knowledgeObjectRepository,
                Clock.systemDefaultZone()
        );
    }

    /**
     * Constructor visible para pruebas deterministas.
     */
    KnowledgeResolverService(
            KnowledgeObjectRepository knowledgeObjectRepository,
            Clock clock
    ) {
        this.knowledgeObjectRepository =
                Objects.requireNonNull(
                        knowledgeObjectRepository,
                        "KnowledgeObjectRepository es obligatorio"
                );

        this.clock = Objects.requireNonNull(
                clock,
                "Clock es obligatorio"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KnowledgeSnapshot> resolve(
            Long storeId,
            String code
    ) {
        return resolveAt(
                storeId,
                code,
                LocalDateTime.now(clock)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KnowledgeSnapshot> resolveAt(
            Long storeId,
            String code,
            LocalDateTime moment
    ) {
        validateStoreId(storeId);

        KnowledgeCode knowledgeCode =
                KnowledgeCode.of(code);

        Objects.requireNonNull(
                moment,
                "El momento de resolución es obligatorio"
        );

        Optional<KnowledgeObject> candidate =
                knowledgeObjectRepository
                        .findPublishedByStoreIdAndCode(
                                storeId,
                                knowledgeCode.getValue(),
                                KnowledgeStatus.PUBLISHED
                        );

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        KnowledgeObject knowledgeObject =
                candidate.get();

        if (!knowledgeObject.isPublishedAt(moment)) {
            return Optional.empty();
        }

        KnowledgeObjectVersion currentVersion =
                knowledgeObject.getCurrentVersion();

        if (currentVersion == null) {
            throw new KnowledgeResolutionException(
                    "KnowledgeObject publicado no tiene versión vigente. "
                            + "storeId="
                            + storeId
                            + ", code="
                            + knowledgeCode.getValue()
            );
        }

        if (!currentVersion.belongsTo(knowledgeObject)) {
            throw new KnowledgeResolutionException(
                    "La versión vigente no pertenece al KnowledgeObject. "
                            + "storeId="
                            + storeId
                            + ", code="
                            + knowledgeCode.getValue()
            );
        }

        return Optional.of(
                toSnapshot(
                        knowledgeObject,
                        currentVersion
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSnapshot require(
            Long storeId,
            String code
    ) {
        return requireAt(
                storeId,
                code,
                LocalDateTime.now(clock)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSnapshot requireAt(
            Long storeId,
            String code,
            LocalDateTime moment
    ) {
        return resolveAt(
                storeId,
                code,
                moment
        ).orElseThrow(
                () -> new KnowledgeResolutionException(
                        "No existe conocimiento publicado y vigente. "
                                + "storeId="
                                + storeId
                                + ", code="
                                + normalizeCodeForMessage(code)
                                + ", moment="
                                + moment
                )
        );
    }

    private static KnowledgeSnapshot toSnapshot(
            KnowledgeObject knowledgeObject,
            KnowledgeObjectVersion version
    ) {
        if (knowledgeObject.getStore() == null
                || knowledgeObject.getStore().getId() == null) {

            throw new KnowledgeResolutionException(
                    "KnowledgeObject no tiene Store válida"
            );
        }

        if (knowledgeObject.getCode() == null) {
            throw new KnowledgeResolutionException(
                    "KnowledgeObject no tiene código"
            );
        }

        if (knowledgeObject.getContextRoot() == null) {
            throw new KnowledgeResolutionException(
                    "KnowledgeObject no tiene contexto"
            );
        }

        if (version.getSemanticVersion() == null) {
            throw new KnowledgeResolutionException(
                    "KnowledgeObjectVersion no tiene versión semántica"
            );
        }

        if (version.getConfidence() == null) {
            throw new KnowledgeResolutionException(
                    "KnowledgeObjectVersion no tiene confianza"
            );
        }

        return new KnowledgeSnapshot(
                knowledgeObject.getId(),
                version.getId(),
                knowledgeObject.getStore().getId(),
                knowledgeObject.getCode().getValue(),
                version.getSemanticVersion().toString(),
                version.getTitle(),
                version.getSummary(),
                version.getContent(),
                version.getContentFormat(),
                version.getConfidence().getValue(),
                version.getSourceReference(),
                knowledgeObject.getTypeCode(),
                knowledgeObject.getDomain(),
                knowledgeObject.getClassification(),
                knowledgeObject.getRiskLevel(),
                knowledgeObject.getContextRoot().getType(),
                knowledgeObject.getContextRoot().getReference(),
                knowledgeObject.getValidFrom(),
                knowledgeObject.getValidUntil(),
                version.getCreatedAt()
        );
    }

    private static void validateStoreId(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de Store debe ser válido"
            );
        }
    }

    private static String normalizeCodeForMessage(
            String code
    ) {
        if (code == null) {
            return "null";
        }

        return code.trim();
    }
}