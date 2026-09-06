package com.webempresarial.store.digitaltransformation.application.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.relationship.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityLink;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreateStrategicRelationshipServiceTest {

    @Mock
    private StrategicArtifactRepository artifactRepository;

    @Mock
    private StrategicRelationshipRepository relationshipRepository;

    @Mock
    private StrategicRelationshipPolicy relationshipPolicy;

    @Mock
    private StrategicCycleDetector cycleDetector;

    @Mock
    private StrategicRelationshipTraceabilitySynchronizer
            traceabilitySynchronizer;

    @Mock
    private StrategicRelationshipProvenanceRecorder
            provenanceRecorder;

    private CreateStrategicRelationshipService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service =
                new CreateStrategicRelationshipService(
                        artifactRepository,
                        relationshipRepository,
                        relationshipPolicy,
                        cycleDetector,
                        traceabilitySynchronizer,
                        provenanceRecorder
                );
    }

    @Test
    void shouldCreateStrategicRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        20L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        CreateStrategicRelationshipCommand command =
                command();

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(Optional.of(finding));

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                20L,
                                100L,
                                1L
                        )
        ).thenReturn(Optional.of(problem));

        when(
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                100L,
                                10L,
                                20L,
                                StrategicRelationshipType.REVEALS
                        )
        ).thenReturn(Optional.empty());

        when(
                cycleDetector.wouldCreateCycle(
                        100L,
                        10L,
                        20L
                )
        ).thenReturn(false);

        when(
                relationshipRepository.save(
                        any(StrategicRelationship.class)
                )
        ).thenAnswer(invocation -> {
            StrategicRelationship relationship =
                    invocation.getArgument(0);

            ReflectionTestUtils.setField(
                    relationship,
                    "id",
                    500L
            );

            return relationship;
        });

        TraceabilityLink traceabilityLink =
                mock(TraceabilityLink.class);

        when(
                traceabilitySynchronizer.synchronize(
                        any(StrategicRelationship.class)
                )
        ).thenReturn(
                new StrategicRelationshipTraceabilitySync(
                        traceabilityLink,
                        true
                )
        );

        CreateStrategicRelationshipResult result =
                service.create(command);

        assertThat(result.created()).isTrue();

        assertThat(result.relationshipId())
                .isEqualTo(500L);

        assertThat(result.sourceArtifactCode())
                .isEqualTo("FND-001");

        assertThat(result.targetArtifactCode())
                .isEqualTo("PRB-001");

        assertThat(result.relationshipType())
                .isEqualTo(
                        StrategicRelationshipType.REVEALS
                );

        verify(relationshipPolicy)
                .validate(
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS
                );

        verify(relationshipRepository)
                .save(
                        any(StrategicRelationship.class)
                );

        verify(traceabilitySynchronizer)
                .synchronize(
                        any(StrategicRelationship.class)
                );

        verify(provenanceRecorder)
                .record(
                        any(StrategicRelationship.class),
                        eq(traceabilityLink)
                );
    }

    @Test
    void shouldReturnExistingRelationshipIdempotently() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        20L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicRelationship existing =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        "Existing relationship"
                );

        ReflectionTestUtils.setField(
                existing,
                "id",
                500L
        );

        stubArtifacts(
                finding,
                problem
        );

        when(
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                100L,
                                10L,
                                20L,
                                StrategicRelationshipType.REVEALS
                        )
        ).thenReturn(
                Optional.of(existing)
        );

        TraceabilityLink traceabilityLink =
                mock(TraceabilityLink.class);

        when(
                traceabilitySynchronizer.synchronize(
                        existing
                )
        ).thenReturn(
                new StrategicRelationshipTraceabilitySync(
                        traceabilityLink,
                        false
                )
        );

        CreateStrategicRelationshipResult result =
                service.create(
                        command()
                );

        assertThat(result.created()).isFalse();

        assertThat(result.relationshipId())
                .isEqualTo(500L);

        verifyNoInteractions(
                cycleDetector
        );

        verify(
                relationshipRepository,
                never()
        ).save(any());

        verify(traceabilitySynchronizer)
                .synchronize(
                        existing
                );

        verifyNoInteractions(
                provenanceRecorder
        );
    }

    @Test
    void shouldRepairTraceabilityForExistingRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        20L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        StrategicRelationship existing =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        "Existing relationship"
                );

        ReflectionTestUtils.setField(
                existing,
                "id",
                500L
        );

        stubArtifacts(
                finding,
                problem
        );

        when(
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                100L,
                                10L,
                                20L,
                                StrategicRelationshipType.REVEALS
                        )
        ).thenReturn(
                Optional.of(existing)
        );

        TraceabilityLink repairedLink =
                mock(TraceabilityLink.class);

        when(
                traceabilitySynchronizer.synchronize(
                        existing
                )
        ).thenReturn(
                new StrategicRelationshipTraceabilitySync(
                        repairedLink,
                        true
                )
        );

        CreateStrategicRelationshipResult result =
                service.create(
                        command()
                );

        assertThat(result.created()).isFalse();

        assertThat(result.relationshipId())
                .isEqualTo(500L);

        verifyNoInteractions(
                cycleDetector
        );

        verify(
                relationshipRepository,
                never()
        ).save(any());

        verify(provenanceRecorder)
                .record(
                        existing,
                        repairedLink
                );
    }

    @Test
    void shouldRejectRelationshipThatCreatesCycle() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        StrategicArtifact problem =
                artifact(
                        evidence,
                        20L,
                        "PRB-001",
                        StrategicArtifactType.BUSINESS_PROBLEM
                );

        stubArtifacts(
                finding,
                problem
        );

        when(
                relationshipRepository
                        .findByProjectIdAndSourceArtifactIdAndTargetArtifactIdAndRelationshipType(
                                100L,
                                10L,
                                20L,
                                StrategicRelationshipType.REVEALS
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                cycleDetector.wouldCreateCycle(
                        100L,
                        10L,
                        20L
                )
        ).thenReturn(true);

        assertThatThrownBy(() ->
                service.create(
                        command()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "ciclo"
                );

        verify(
                relationshipRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                traceabilitySynchronizer,
                provenanceRecorder
        );
    }

    @Test
    void shouldFailWhenSourceArtifactDoesNotExist() {
        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                service.create(
                        command()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "origen"
                );

        verifyNoInteractions(
                relationshipRepository,
                relationshipPolicy,
                cycleDetector,
                traceabilitySynchronizer,
                provenanceRecorder
        );
    }

    @Test
    void shouldFailWhenTargetArtifactDoesNotExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicArtifact finding =
                artifact(
                        evidence,
                        10L,
                        "FND-001",
                        StrategicArtifactType.FINDING
                );

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(finding)
        );

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                20L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(() ->
                service.create(
                        command()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "destino"
                );

        verifyNoInteractions(
                relationshipRepository,
                relationshipPolicy,
                cycleDetector,
                traceabilitySynchronizer,
                provenanceRecorder
        );
    }

    private void stubArtifacts(
            StrategicArtifact finding,
            StrategicArtifact problem
    ) {
        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                10L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(finding)
        );

        when(
                artifactRepository
                        .findByIdAndProjectIdAndProjectStoreId(
                                20L,
                                100L,
                                1L
                        )
        ).thenReturn(
                Optional.of(problem)
        );
    }

    private static CreateStrategicRelationshipCommand command() {
        return new CreateStrategicRelationshipCommand(
                1L,
                100L,
                10L,
                20L,
                StrategicRelationshipType.REVEALS,
                StrategicRelationshipOrigin.RULE_ENGINE,
                "El hallazgo revela el problema de negocio."
        );
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            Long id,
            String code,
            StrategicArtifactType type
    ) {
        if (evidence.getProject().getId() == null) {
            ReflectionTestUtils.setField(
                    evidence.getProject(),
                    "id",
                    100L
            );
        }

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        code,
                        type,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        "Statement " + code,
                        "Rationale " + code,
                        null
                );

        ReflectionTestUtils.setField(
                artifact,
                "id",
                id
        );

        return artifact;
    }
}