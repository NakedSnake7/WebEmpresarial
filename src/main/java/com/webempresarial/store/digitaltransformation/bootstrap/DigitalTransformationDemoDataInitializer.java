package com.webempresarial.store.digitaltransformation.bootstrap;

import com.webempresarial.store.digitaltransformation.application.evidence.traceability.RegisterEvidenceIntoTraceabilityService;
import com.webempresarial.store.digitaltransformation.application.project.CreateTransformationProjectCommand;
import com.webempresarial.store.digitaltransformation.application.project.CreateTransformationProjectService;
import com.webempresarial.store.digitaltransformation.application.strategic.derivation.DeriveStrategicArtifactFromEvidenceService;
import com.webempresarial.store.digitaltransformation.application.strategic.relationship.CreateStrategicRelationshipCommand;
import com.webempresarial.store.digitaltransformation.application.strategic.relationship.CreateStrategicRelationshipService;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.project.*;
import com.webempresarial.store.digitaltransformation.domain.source.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@Profile("dev")
@ConditionalOnProperty(
        prefix = "webempresarial.demo.digital-transformation",
        name = "enabled",
        havingValue = "true"
)
public class DigitalTransformationDemoDataInitializer
        implements ApplicationRunner {

    private static final String PROJECT_CODE =
            "DTE-DEMO-001";

    private static final String CHECKSUM =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
          + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private final StoreRepository storeRepository;
    private final TransformationProjectRepository projectRepository;
    private final TransformationSourceDocumentRepository sourceRepository;
    private final SourceDocumentContentRepository contentRepository;
    private final SourceEvidenceRepository evidenceRepository;
    private final StrategicArtifactRepository artifactRepository;
    private final StrategicRelationshipRepository relationshipRepository;

    private final CreateTransformationProjectService projectService;
    private final DeriveStrategicArtifactFromEvidenceService
    derivationService;
    private final CreateStrategicRelationshipService relationshipService;
    
    private final RegisterEvidenceIntoTraceabilityService
    evidenceTraceabilityService;

    public DigitalTransformationDemoDataInitializer(
            StoreRepository storeRepository,
            TransformationProjectRepository projectRepository,
            TransformationSourceDocumentRepository sourceRepository,
            SourceDocumentContentRepository contentRepository,
            SourceEvidenceRepository evidenceRepository,
            StrategicArtifactRepository artifactRepository,
            StrategicRelationshipRepository relationshipRepository,
            CreateTransformationProjectService projectService,
            RegisterEvidenceIntoTraceabilityService evidenceTraceabilityService,
            DeriveStrategicArtifactFromEvidenceService derivationService,
            CreateStrategicRelationshipService relationshipService
    ) {
        this.storeRepository =
                Objects.requireNonNull(
                        storeRepository,
                        "StoreRepository es obligatorio"
                );

        this.projectRepository =
                Objects.requireNonNull(
                        projectRepository,
                        "TransformationProjectRepository es obligatorio"
                );

        this.sourceRepository =
                Objects.requireNonNull(
                        sourceRepository,
                        "TransformationSourceDocumentRepository es obligatorio"
                );

        this.contentRepository =
                Objects.requireNonNull(
                        contentRepository,
                        "SourceDocumentContentRepository es obligatorio"
                );

        this.evidenceRepository =
                Objects.requireNonNull(
                        evidenceRepository,
                        "SourceEvidenceRepository es obligatorio"
                );

        this.artifactRepository =
                Objects.requireNonNull(
                        artifactRepository,
                        "StrategicArtifactRepository es obligatorio"
                );

        this.relationshipRepository =
                Objects.requireNonNull(
                        relationshipRepository,
                        "StrategicRelationshipRepository es obligatorio"
                );

        this.projectService =
                Objects.requireNonNull(
                        projectService,
                        "CreateTransformationProjectService es obligatorio"
                );

        this.evidenceTraceabilityService =
                Objects.requireNonNull(
                        evidenceTraceabilityService,
                        "RegisterEvidenceIntoTraceabilityService es obligatorio"
                );

        this.derivationService =
                Objects.requireNonNull(
                        derivationService,
                        "DeriveStrategicArtifactFromEvidenceService es obligatorio"
                );

        this.relationshipService =
                Objects.requireNonNull(
                        relationshipService,
                        "CreateStrategicRelationshipService es obligatorio"
                );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        Store store = resolveStore();

        TransformationProject project =
                resolveProject(store);

        TransformationSourceDocument source =
                resolveSource(project);

        resolveContent(source);

        StrategicArtifact finding =
                resolveStrategicArtifact(
                        project,
                        source,
                        "EVD-001",
                        EvidenceClassification.STRATEGIC_FINDING,
                        StrategicArtifactType.FINDING,
                        "La experiencia digital actual presenta "
                                + "fragmentación entre información, "
                                + "procesos y puntos de contacto.",
                        "La auditoría identifica una experiencia "
                                + "digital fragmentada que dificulta "
                                + "la coherencia del recorrido del cliente.",
                        "La fragmentación constituye un hallazgo "
                                + "estratégico que requiere consolidación.",
                        1
                );

        StrategicArtifact problem =
                resolveStrategicArtifact(
                        project,
                        source,
                        "EVD-002",
                        EvidenceClassification.BUSINESS_PROBLEM,
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        "La fragmentación tecnológica incrementa "
                                + "la fricción operativa y limita "
                                + "la capacidad de escalar.",
                        "Los procesos y activos digitales separados "
                                + "generan duplicidad, fricción y "
                                + "dificultan la evolución del negocio.",
                        "La organización necesita reducir la "
                                + "fragmentación operativa.",
                        2
                );

        StrategicArtifact objective =
                resolveStrategicArtifact(
                        project,
                        source,
                        "EVD-003",
                        EvidenceClassification.BUSINESS_OBJECTIVE,
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        "Consolidar la experiencia digital y los "
                                + "procesos críticos dentro de una "
                                + "plataforma coherente.",
                        "La transformación propuesta busca una "
                                + "experiencia digital consistente, "
                                + "administrable y escalable.",
                        "La consolidación permite mejorar operación "
                                + "y experiencia del cliente.",
                        3
                );

        StrategicArtifact opportunity =
                resolveStrategicArtifact(
                        project,
                        source,
                        "EVD-004",
                        EvidenceClassification.STRATEGIC_OPPORTUNITY,
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                        "Construir una experiencia digital integrada "
                                + "que conecte operación, conocimiento "
                                + "y experiencia del cliente.",
                        "La plataforma integrada permite transformar "
                                + "la fragmentación actual en una "
                                + "capacidad digital reutilizable.",
                        "Existe una oportunidad para convertir la "
                                + "plataforma digital en una capacidad "
                                + "estratégica del negocio.",
                        4
                );

        ensureRelationship(
                store,
                project,
                finding,
                problem,
                StrategicRelationshipType.REVEALS,
                "El finding revela directamente el problema "
                        + "de fragmentación operativa."
        );

        ensureRelationship(
                store,
                project,
                problem,
                objective,
                StrategicRelationshipType.ADDRESSED_BY,
                "El objetivo de consolidación aborda directamente "
                        + "el problema identificado."
        );

        ensureRelationship(
                store,
                project,
                objective,
                opportunity,
                StrategicRelationshipType.ENABLES,
                "El objetivo habilita la oportunidad de construir "
                        + "una experiencia digital integrada."
        );
    }

    private Store resolveStore() {

        List<Store> stores =
                storeRepository.findAll();

        if (stores.isEmpty()) {
            throw new IllegalStateException(
                    "No existe ningún Store para crear "
                            + "el proyecto demo de Digital Transformation"
            );
        }

        /*
         * Temporalmente usamos el primer Store disponible.
         *
         * Más adelante podemos reemplazar esto por una property
         * explícita store-id/store-code.
         */
        return stores.getFirst();
    }

    private TransformationProject resolveProject(
            Store store
    ) {
        return projectRepository
                .findByStoreIdAndCodeIgnoreCase(
                        store.getId(),
                        PROJECT_CODE
                )
                .orElseGet(() -> {

                    projectService.create(
                            new CreateTransformationProjectCommand(
                                    store.getId(),
                                    PROJECT_CODE,
                                    "Digital Transformation Engine Demo",
                                    "WebEmpresarial Demo Client",
                                    "https://example.com",
                                    TransformationProjectType.WEBSITE_TRANSFORMATION,
                                    "Demostrar el flujo completo de "
                                            + "Strategic Intelligence desde "
                                            + "evidencia hasta governance."
                            )
                    );

                    return projectRepository
                            .findByStoreIdAndCodeIgnoreCase(
                                    store.getId(),
                                    PROJECT_CODE
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "El proyecto demo fue creado "
                                                    + "pero no pudo recuperarse"
                                    )
                            );
                });
    }

    private TransformationSourceDocument resolveSource(
            TransformationProject project
    ) {

        return sourceRepository
                .findAllByProjectIdAndSourceRoleOrderByRegisteredAtAsc(
                        project.getId(),
                        TransformationSourceRole.SOURCE_OF_TRUTH
                )
                .stream()
                .filter(source ->
                        source.getSourceType()
                                == TransformationSourceType.DIGITAL_EXCELLENCE_AUDIT
                )
                .findFirst()
                .orElseGet(() -> {

                    TransformationSourceDocument source =
                            TransformationSourceDocument.register(
                                    project,
                                    TransformationSourceType.DIGITAL_EXCELLENCE_AUDIT,
                                    TransformationSourceRole.SOURCE_OF_TRUTH,
                                    "digital-excellence-audit-demo.pdf",
                                    "Digital Excellence Audit — Demo",
                                    "application/pdf",
                                    "demo://digital-transformation/"
                                            + PROJECT_CODE
                                            + "/audit.pdf",
                                    CHECKSUM,
                                    1,
                                    "es",
                                    10
                            );

                    source.markUploaded();
                    source.markParsed();
                    source.markAnalyzed();
                    source.verify();

                    return sourceRepository.save(source);
                });
    }

    private SourceDocumentContent resolveContent(
            TransformationSourceDocument source
    ) {

        return contentRepository
                .findBySourceDocumentIdAndCurrentTrue(
                        source.getId()
                )
                .orElseGet(() -> {

                    SourceDocumentContent content =
                            SourceDocumentContent.create(
                                    source,
                                    1,
                                    SourceContentExtractionMethod.MANUAL,
                                    "DigitalTransformationDemoInitializer",
                                    "1.0"
                            );

                    content.startExtraction();

                    content.completeExtraction(
                            """
                            La auditoría identifica fragmentación en la
                            experiencia digital y oportunidades para
                            consolidar procesos, información y puntos
                            de contacto dentro de una plataforma
                            coherente, escalable y administrable.
                            """,
                            "es"
                    );

                    content.verify();
                    content.markCurrent();

                    return contentRepository.save(content);
                });
    }

    private StrategicArtifact resolveStrategicArtifact(
            TransformationProject project,
            TransformationSourceDocument source,
            String evidenceCode,
            EvidenceClassification classification,
            StrategicArtifactType expectedType,
            String statement,
            String supportingExcerpt,
            String interpretation,
            int page
    ) {

    	SourceEvidence evidence =
    	        evidenceRepository
    	                .findAllByProjectIdOrderByExtractedAtAsc(
    	                        project.getId()
    	                )
    	                .stream()
    	                .filter(candidate ->
    	                        evidenceCode.equals(
    	                                candidate.getEvidenceCode()
    	                        )
    	                )
    	                .findFirst()
    	                .orElseGet(() -> {

    	                    SourceEvidence created =
    	                            SourceEvidence.extract(
    	                                    project,
    	                                    source,
    	                                    null,
    	                                    evidenceCode,
    	                                    classification,
    	                                    EvidenceConfidence.EXPLICIT,
    	                                    EvidenceExtractionOrigin.MANUAL,
    	                                    statement,
    	                                    supportingExcerpt,
    	                                    interpretation,
    	                                    EvidenceLocator.page(page)
    	                            );

    	                    created.verify(
    	                            "Digital Transformation Demo Initializer"
    	                    );

    	                    return evidenceRepository.save(
    	                            created
    	                    );
    	                });

    	evidenceTraceabilityService.register(
    	        project.getStore().getId(),
    	        evidence.getId(),
    	        "Digital Transformation Demo Initializer"
    	);

    	StrategicArtifact artifact =
    	        artifactRepository
    	                .findBySourceEvidenceIdAndArtifactType(
    	                        evidence.getId(),
    	                        expectedType
    	                )
    	                .orElseGet(() -> {

    	                    derivationService.derive(
    	                            project.getStore().getId(),
    	                            evidence.getId()
    	                    );

    	                    return artifactRepository
    	                            .findBySourceEvidenceIdAndArtifactType(
    	                                    evidence.getId(),
    	                                    expectedType
    	                            )
    	                            .orElseThrow(() ->
    	                                    new IllegalStateException(
    	                                            "La derivación estratégica no produjo "
    	                                                    + "el artefacto esperado "
    	                                                    + expectedType
    	                                                    + " para la evidencia "
    	                                                    + evidenceCode
    	                                    )
    	                            );
    	                });

        if (artifact.getStatus()
                == StrategicArtifactStatus.DRAFT
                || artifact.getStatus()
                == StrategicArtifactStatus.REVIEW_REQUIRED) {

            artifact.verify(
                    "Digital Transformation Demo Initializer"
            );

            artifact =
                    artifactRepository.save(artifact);
        }

        return artifact;
    }

    private void ensureRelationship(
            Store store,
            TransformationProject project,
            StrategicArtifact source,
            StrategicArtifact target,
            StrategicRelationshipType type,
            String rationale
    ) {

        boolean exists =
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                project.getId(),
                                source.getId(),
                                target.getId(),
                                type
                        )
                        .isPresent();

        if (exists) {
            return;
        }

        relationshipService.create(
                new CreateStrategicRelationshipCommand(
                        store.getId(),
                        project.getId(),
                        source.getId(),
                        target.getId(),
                        type,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        rationale
                )
        );
    }
}