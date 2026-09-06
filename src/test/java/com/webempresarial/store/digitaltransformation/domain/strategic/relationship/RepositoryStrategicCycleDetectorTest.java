package com.webempresarial.store.digitaltransformation.domain.strategic.relationship;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RepositoryStrategicCycleDetectorTest {

    @Mock
    private StrategicRelationshipRepository repository;

    private RepositoryStrategicCycleDetector detector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        detector =
                new RepositoryStrategicCycleDetector(
                        repository
                );
    }

    @Test
    void shouldDetectDirectCycle() {
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
                        null
                );

        /*
         * Existe 10 -> 20.
         * Intentar 20 -> 10 debe producir ciclo.
         */
        when(
                repository
                        .findAllByProjectIdAndSourceArtifactId(
                                evidence.getProject().getId(),
                                10L
                        )
        ).thenReturn(
                List.of(existing)
        );

        assertThat(
                detector.wouldCreateCycle(
                        evidence.getProject().getId(),
                        20L,
                        10L
                )
        ).isTrue();
    }

    @Test
    void shouldDetectTransitiveCycle() {
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

        StrategicArtifact objective =
                artifact(
                        evidence,
                        30L,
                        "OBJ-001",
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        StrategicRelationship first =
                StrategicRelationship.create(
                        evidence.getProject(),
                        finding,
                        problem,
                        StrategicRelationshipType.REVEALS,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        StrategicRelationship second =
                StrategicRelationship.create(
                        evidence.getProject(),
                        problem,
                        objective,
                        StrategicRelationshipType.ADDRESSED_BY,
                        StrategicRelationshipOrigin.RULE_ENGINE,
                        null
                );

        when(
                repository
                        .findAllByProjectIdAndSourceArtifactId(
                                evidence.getProject().getId(),
                                10L
                        )
        ).thenReturn(
                List.of(first)
        );

        when(
                repository
                        .findAllByProjectIdAndSourceArtifactId(
                                evidence.getProject().getId(),
                                20L
                        )
        ).thenReturn(
                List.of(second)
        );

        assertThat(
                detector.wouldCreateCycle(
                        evidence.getProject().getId(),
                        30L,
                        10L
                )
        ).isTrue();
    }

    @Test
    void shouldAllowAcyclicRelationship() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        when(
                repository
                        .findAllByProjectIdAndSourceArtifactId(
                                evidence.getProject().getId(),
                                20L
                        )
        ).thenReturn(
                List.of()
        );

        assertThat(
                detector.wouldCreateCycle(
                        evidence.getProject().getId(),
                        10L,
                        20L
                )
        ).isFalse();
    }

    private static StrategicArtifact artifact(
            SourceEvidence evidence,
            Long id,
            String code,
            StrategicArtifactType type
    ) {
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