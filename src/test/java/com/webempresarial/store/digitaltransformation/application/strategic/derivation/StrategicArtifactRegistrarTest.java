package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactCodeGenerator;
import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactTypeDescriptor;
import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationDecision;
import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationResult;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactOrigin;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StrategicArtifactRegistrarTest {

    @Mock
    private StrategicArtifactRepository artifactRepository;

    @Mock
    private StrategicArtifactCodeGenerator codeGenerator;

    private StrategicArtifactRegistrar registrar;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registrar =
                new StrategicArtifactRegistrar(
                        artifactRepository,
                        codeGenerator
                );
    }

    @Test
    void shouldReturnExistingArtifactInsteadOfCreatingDuplicate() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicClassificationResult classification =
                findingClassification();

        StrategicArtifact existingArtifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        "FND-001",
                        StrategicArtifactType.FINDING,
                        StrategicConfidence.EXPLICIT,
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        evidence.getStatement(),
                        evidence.getInterpretation(),
                        null
                );

        when(
                artifactRepository
                        .findBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                Optional.of(existingArtifact)
        );

        StrategicArtifact result =
                registrar.register(
                        evidence,
                        classification
                );

        assertThat(result)
                .isSameAs(existingArtifact);

        verify(
                artifactRepository
        ).findBySourceEvidenceIdAndArtifactType(
                evidence.getId(),
                StrategicArtifactType.FINDING
        );

        verify(
                artifactRepository,
                never()
        ).countByProjectIdAndArtifactType(
                any(),
                any()
        );

        verifyNoInteractions(
                codeGenerator
        );

        verify(
                artifactRepository,
                never()
        ).save(
                any(StrategicArtifact.class)
        );
    }

    @Test
    void shouldCreateArtifactWhenDerivationDoesNotExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicClassificationResult classification =
                findingClassification();

        when(
                artifactRepository
                        .findBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                evidence.getProject().getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                0L
        );

        when(
                codeGenerator.generate(
                        eq(
                                StrategicArtifactTypeDescriptor.of(
                                        StrategicArtifactType.FINDING
                                )
                        ),
                        eq(1L)
                )
        ).thenReturn(
                "FND-001"
        );

        when(
                artifactRepository.save(
                        any(StrategicArtifact.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        StrategicArtifact result =
                registrar.register(
                        evidence,
                        classification
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getArtifactCode())
                .isEqualTo(
                        "FND-001"
                );

        assertThat(result.getArtifactType())
                .isEqualTo(
                        StrategicArtifactType.FINDING
                );

        assertThat(result.getConfidence())
                .isEqualTo(
                        StrategicConfidence.EXPLICIT
                );

        assertThat(result.getOrigin())
                .isEqualTo(
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        StrategicArtifactStatus.DRAFT
                );

        assertThat(result.getStatement())
                .isEqualTo(
                        evidence.getStatement()
                );

        assertThat(result.getRationale())
                .isEqualTo(
                        evidence.getInterpretation()
                );

        assertThat(result.getBusinessImplication())
                .isNull();

        assertThat(result.getSourceEvidence())
                .isSameAs(
                        evidence
                );

        verify(
                artifactRepository
        ).findBySourceEvidenceIdAndArtifactType(
                evidence.getId(),
                StrategicArtifactType.FINDING
        );

        verify(
                artifactRepository
        ).countByProjectIdAndArtifactType(
                evidence.getProject().getId(),
                StrategicArtifactType.FINDING
        );

        verify(
                codeGenerator
        ).generate(
                eq(
                        StrategicArtifactTypeDescriptor.of(
                                StrategicArtifactType.FINDING
                        )
                ),
                eq(1L)
        );

        verify(
                artifactRepository
        ).save(
                any(StrategicArtifact.class)
        );
    }

    @Test
    void shouldUseNextSequenceWhenArtifactsAlreadyExist() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicClassificationResult classification =
                findingClassification();

        when(
                artifactRepository
                        .findBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                evidence.getProject().getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                7L
        );

        when(
                codeGenerator.generate(
                        eq(
                                StrategicArtifactTypeDescriptor.of(
                                        StrategicArtifactType.FINDING
                                )
                        ),
                        eq(8L)
                )
        ).thenReturn(
                "FND-008"
        );

        when(
                artifactRepository.save(
                        any(StrategicArtifact.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        StrategicArtifact result =
                registrar.register(
                        evidence,
                        classification
                );

        assertThat(result.getArtifactCode())
                .isEqualTo(
                        "FND-008"
                );

        verify(
                codeGenerator
        ).generate(
                eq(
                        StrategicArtifactTypeDescriptor.of(
                                StrategicArtifactType.FINDING
                        )
                ),
                eq(8L)
        );
    }

    @Test
    void shouldPreserveClassificationConfidence() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicClassificationResult classification =
                classification(
                        StrategicArtifactType.BUSINESS_OBJECTIVE,
                        StrategicConfidence.STRONGLY_SUPPORTED
                );

        when(
                artifactRepository
                        .findBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.BUSINESS_OBJECTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                evidence.getProject().getId(),
                                StrategicArtifactType.BUSINESS_OBJECTIVE
                        )
        ).thenReturn(
                0L
        );

        when(
                codeGenerator.generate(
                        eq(
                                StrategicArtifactTypeDescriptor.of(
                                        StrategicArtifactType.BUSINESS_OBJECTIVE
                                )
                        ),
                        eq(1L)
                )
        ).thenReturn(
                "OBJ-001"
        );

        when(
                artifactRepository.save(
                        any(StrategicArtifact.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        StrategicArtifact result =
                registrar.register(
                        evidence,
                        classification
                );

        assertThat(result.getArtifactType())
                .isEqualTo(
                        StrategicArtifactType.BUSINESS_OBJECTIVE
                );

        assertThat(result.getConfidence())
                .isEqualTo(
                        StrategicConfidence.STRONGLY_SUPPORTED
                );

        assertThat(result.getArtifactCode())
                .isEqualTo(
                        "OBJ-001"
                );
    }

    @Test
    void shouldCreateArtifactWithEvidenceDerivationOrigin() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicClassificationResult classification =
                findingClassification();

        when(
                artifactRepository
                        .findBySourceEvidenceIdAndArtifactType(
                                evidence.getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                evidence.getProject().getId(),
                                StrategicArtifactType.FINDING
                        )
        ).thenReturn(
                0L
        );

        when(
                codeGenerator.generate(
                        any(StrategicArtifactTypeDescriptor.class),
                        eq(1L)
                )
        ).thenReturn(
                "FND-001"
        );

        when(
                artifactRepository.save(
                        any(StrategicArtifact.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        StrategicArtifact result =
                registrar.register(
                        evidence,
                        classification
                );

        assertThat(result.getOrigin())
                .isEqualTo(
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION
                );
    }

    private static StrategicClassificationResult
    findingClassification() {
        return classification(
                StrategicArtifactType.FINDING,
                StrategicConfidence.EXPLICIT
        );
    }

    private static StrategicClassificationResult classification(
            StrategicArtifactType type,
            StrategicConfidence confidence
    ) {
        return new StrategicClassificationResult(
                type,
                confidence,
                StrategicClassificationDecision.AUTO_ACCEPT,
                10,
                2,
                "Clasificación estratégica suficientemente respaldada",
                List.of(),
                false,
                true
        );
    }
}