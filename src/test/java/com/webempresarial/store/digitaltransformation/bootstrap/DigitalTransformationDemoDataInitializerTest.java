package com.webempresarial.store.digitaltransformation.bootstrap;

import com.webempresarial.store.digitaltransformation.application.evidence.traceability.RegisterEvidenceIntoTraceabilityService;
import com.webempresarial.store.digitaltransformation.application.project.CreateTransformationProjectService;
import com.webempresarial.store.digitaltransformation.application.strategic.relationship.CreateStrategicRelationshipService;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicGraphTraversalEngine;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidenceRepository;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectRepository;

import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentContent;
import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentContentRepository;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocumentRepository;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;

import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationship;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.StrategicRelationshipType;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalResult;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicTraversalStatus;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.webempresarial.store.digitaltransformation.application.strategic.derivation.DeriveStrategicArtifactFromEvidenceService;
@SpringBootTest
@Transactional
class DigitalTransformationDemoDataInitializerTest {

    private static final String PROJECT_CODE =
            "DTE-DEMO-001";

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TransformationProjectRepository projectRepository;

    @Autowired
    private TransformationSourceDocumentRepository sourceRepository;

    @Autowired
    private SourceDocumentContentRepository contentRepository;

    @Autowired
    private SourceEvidenceRepository evidenceRepository;

    @Autowired
    private StrategicArtifactRepository artifactRepository;

    @Autowired
    private StrategicRelationshipRepository relationshipRepository;

    @Autowired
    private CreateTransformationProjectService projectService;


    @Autowired
    private CreateStrategicRelationshipService relationshipService;

    @Autowired
    private StrategicGraphTraversalEngine traversalEngine;
    
    @Autowired
    private DeriveStrategicArtifactFromEvidenceService
            derivationService;
    
    @Autowired
    private RegisterEvidenceIntoTraceabilityService
            evidenceTraceabilityService;

    private DigitalTransformationDemoDataInitializer initializer;

    @BeforeEach
    void setUp() {
    	initializer =
    	        new DigitalTransformationDemoDataInitializer(
    	                storeRepository,
    	                projectRepository,
    	                sourceRepository,
    	                contentRepository,
    	                evidenceRepository,
    	                artifactRepository,
    	                relationshipRepository,
    	                projectService,
    	                evidenceTraceabilityService,
    	                derivationService,
    	                relationshipService
    	        );
    }

    @Test
    void shouldCreateCompleteDemoDataset() throws Exception {

        Store store =
                requireStore();

        initializer.run(
                emptyArguments()
        );

        TransformationProject project =
                findDemoProject(
                        store
                );

        /*
         * PROJECT
         */
        assertThat(project.getCode())
                .isEqualTo(
                        PROJECT_CODE
                );

        assertThat(project.getStore().getId())
                .isEqualTo(
                        store.getId()
                );

        /*
         * SOURCE
         */
        List<TransformationSourceDocument> sources =
                sourceRepository
                        .findAllByProjectIdOrderByRegisteredAtAsc(
                                project.getId()
                        );

        assertThat(sources)
                .hasSize(1);

        TransformationSourceDocument source =
                sources.getFirst();

        assertThat(source.isVerifiedAuthoritativeSource())
                .isTrue();

        /*
         * CONTENT
         */
        List<SourceDocumentContent> contents =
                contentRepository
                        .findAllBySourceDocumentIdOrderByContentVersionDesc(
                                source.getId()
                        );

        assertThat(contents)
                .hasSize(1);

        SourceDocumentContent content =
                contents.getFirst();

        assertThat(content.isCurrent())
                .isTrue();

        assertThat(content.getRawText())
                .isNotBlank();

        /*
         * EVIDENCE
         */
        List<SourceEvidence> evidence =
                evidenceRepository
                        .findAllByProjectIdOrderByExtractedAtAsc(
                                project.getId()
                        );

        assertThat(evidence)
                .hasSize(4);

        assertThat(
                evidence.stream()
                        .map(
                                SourceEvidence::getEvidenceCode
                        )
                        .toList()
        )
                .containsExactlyInAnyOrder(
                        "EVD-001",
                        "EVD-002",
                        "EVD-003",
                        "EVD-004"
                );

        assertThat(evidence)
                .allMatch(
                        item ->
                                !item.isRequiresHumanReview()
                );

        /*
         * STRATEGIC ARTIFACTS
         */
        List<StrategicArtifact> artifacts =
                artifactRepository
                        .findAllByProjectIdOrderByCreatedAtAsc(
                                project.getId()
                        );

        assertThat(artifacts)
                .hasSize(4);

        assertThat(
                artifacts.stream()
                        .map(
                                StrategicArtifact::getArtifactType
                        )
                        .toList()
        )
                .containsExactlyInAnyOrder(
                        StrategicArtifactType.FINDING,
                        StrategicArtifactType.BUSINESS_PROBLEM,
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        assertThat(artifacts)
                .allMatch(
                        artifact ->
                                artifact.getStatus()
                                        == StrategicArtifactStatus.VERIFIED
                );

        /*
         * RELATIONSHIPS
         */
        List<StrategicRelationship> relationships =
                relationshipsForProject(
                        project.getId()
                );

        assertThat(relationships)
                .hasSize(3);

        assertThat(
                relationships.stream()
                        .map(
                                StrategicRelationship::getRelationshipType
                        )
                        .toList()
        )
                .containsExactlyInAnyOrder(
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipType.ADDRESSED_BY,
                        StrategicRelationshipType.ENABLES
                );

        assertThat(relationships)
                .allMatch(
                        StrategicRelationship::isActive
                );
    }

    @Test
    void shouldBeIdempotentWhenExecutedMoreThanOnce()
            throws Exception {

        Store store =
                requireStore();

        initializer.run(
                emptyArguments()
        );

        initializer.run(
                emptyArguments()
        );

        TransformationProject project =
                findDemoProject(
                        store
                );

        long projectCount =
                projectRepository
                        .findAllByStoreIdOrderByCreatedAtDesc(
                                store.getId()
                        )
                        .stream()
                        .filter(candidate ->
                                PROJECT_CODE.equals(
                                        candidate.getCode()
                                )
                        )
                        .count();

        assertThat(projectCount)
                .isEqualTo(1);

        assertThat(
                sourceRepository
                        .findAllByProjectIdOrderByRegisteredAtAsc(
                                project.getId()
                        )
        )
                .hasSize(1);

        TransformationSourceDocument source =
                sourceRepository
                        .findAllByProjectIdOrderByRegisteredAtAsc(
                                project.getId()
                        )
                        .getFirst();

        assertThat(
                contentRepository
                        .findAllBySourceDocumentIdOrderByContentVersionDesc(
                                source.getId()
                        )
        )
                .hasSize(1);

        assertThat(
                evidenceRepository
                        .findAllByProjectIdOrderByExtractedAtAsc(
                                project.getId()
                        )
        )
                .hasSize(4);

        assertThat(
                artifactRepository
                        .findAllByProjectIdOrderByCreatedAtAsc(
                                project.getId()
                        )
        )
                .hasSize(4);

        assertThat(
                relationshipsForProject(
                        project.getId()
                )
        )
                .hasSize(3);
    }

    @Test
    void shouldProduceCompleteStrategicTraversal()
            throws Exception {

        Store store =
                requireStore();

        initializer.run(
                emptyArguments()
        );

        TransformationProject project =
                findDemoProject(
                        store
                );

        StrategicArtifact finding =
                artifactRepository
                        .findAllByProjectIdAndArtifactTypeOrderByCreatedAtAsc(
                                project.getId(),
                                StrategicArtifactType.FINDING
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "El fixture demo no creó un FINDING"
                                )
                        );

        StrategicTraversalResult traversal =
                traversalEngine
                        .traverseFromFinding(
                                store.getId(),
                                project.getId(),
                                finding.getId()
                        );

        assertThat(traversal)
                .isNotNull();

        assertThat(traversal.getStatus())
                .isEqualTo(
                        StrategicTraversalStatus.COMPLETE
                );

        assertThat(traversal.isComplete())
                .isTrue();

        assertThat(traversal.isAmbiguous())
                .isFalse();

        assertThat(traversal.getFinding())
                .isNotNull();

        assertThat(traversal.getFinding().getArtifactType())
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );

        assertThat(traversal.getBusinessProblem())
                .isNotNull();

        assertThat(
                traversal
                        .getBusinessProblem()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        assertThat(traversal.getBusinessObjective())
                .isNotNull();

        assertThat(
                traversal
                        .getBusinessObjective()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        assertThat(traversal.getStrategicOpportunity())
                .isNotNull();

        assertThat(
                traversal
                        .getStrategicOpportunity()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );

        assertThat(traversal.getGaps())
                .isEmpty();

        assertThat(traversal.getAmbiguities())
                .isEmpty();
    }

    @Test
    void shouldCreateExactlyExpectedStrategicPath()
            throws Exception {

        Store store =
                requireStore();

        initializer.run(
                emptyArguments()
        );

        TransformationProject project =
                findDemoProject(
                        store
                );

        List<StrategicRelationship> relationships =
                relationshipsForProject(
                        project.getId()
                );

        StrategicRelationship reveals =
                relationship(
                        relationships,
                        StrategicRelationshipType.REVEALS
                );

        assertThat(
                reveals
                        .getSourceArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );

        assertThat(
                reveals
                        .getTargetArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicRelationship addressedBy =
                relationship(
                        relationships,
                        StrategicRelationshipType.ADDRESSED_BY
                );

        assertThat(
                addressedBy
                        .getSourceArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        assertThat(
                addressedBy
                        .getTargetArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicRelationship enables =
                relationship(
                        relationships,
                        StrategicRelationshipType.ENABLES
                );

        assertThat(
                enables
                        .getSourceArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        assertThat(
                enables
                        .getTargetArtifact()
                        .getArtifactType()
        )
                .isEqualTo(
                        StrategicArtifactType.STRATEGIC_OPPORTUNITY
                );
    }

    private Store requireStore() {

        return storeRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "DigitalTransformationDemoDataInitializerTest "
                                        + "requiere al menos un Store en "
                                        + "el entorno de pruebas"
                        )
                );
    }

    private TransformationProject findDemoProject(
            Store store
    ) {

        return projectRepository
                .findByStoreIdAndCodeIgnoreCase(
                        store.getId(),
                        PROJECT_CODE
                )
                .orElseThrow(() ->
                        new AssertionError(
                                "No se creó el proyecto "
                                        + PROJECT_CODE
                        )
                );
    }

    private List<StrategicRelationship>
    relationshipsForProject(
            Long projectId
    ) {

        /*
         * Usamos findAll() para no depender de un método
         * adicional del repository que todavía no necesitamos
         * en producción.
         */
        return relationshipRepository
                .findAll()
                .stream()
                .filter(relationship ->
                        relationship.getProject() != null
                                && projectId.equals(
                                        relationship
                                                .getProject()
                                                .getId()
                                )
                )
                .toList();
    }

    private static StrategicRelationship relationship(
            List<StrategicRelationship> relationships,
            StrategicRelationshipType type
    ) {

        return relationships
                .stream()
                .filter(relationship ->
                        relationship.getRelationshipType()
                                == type
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "No existe la relación estratégica "
                                        + type
                        )
                );
    }

    private static DefaultApplicationArguments
    emptyArguments() {

        return new DefaultApplicationArguments(
                new String[0]
        );
    }
}