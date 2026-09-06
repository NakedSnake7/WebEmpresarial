package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultStrategicInterpretationTraceabilityRecorderTest {

    @Mock
    private StrategicSynthesisTraceabilityRegistrar
            synthesisTraceabilityRegistrar;

    @Mock
    private TraceabilityLinkRepository
            traceabilityLinkRepository;

    @Mock
    private TraceabilityNode aiNode;

    @Mock
    private TraceabilityNode deterministicNode;

    private DefaultStrategicInterpretationTraceabilityRecorder
            recorder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recorder =
                new DefaultStrategicInterpretationTraceabilityRecorder(
                        synthesisTraceabilityRegistrar,
                        traceabilityLinkRepository
                );
    }
    
    @Test
    void shouldPersistInterpretationAuditContextInTraceabilityRationale() {

        Context context =
                context();

        when(
                synthesisTraceabilityRegistrar.register(
                        context.aiSynthesis()
                )
        ).thenReturn(
                aiNode
        );

        when(
                synthesisTraceabilityRegistrar.register(
                        context.sourceSynthesis()
                )
        ).thenReturn(
                deterministicNode
        );

        when(aiNode.getId())
                .thenReturn(200L);

        when(deterministicNode.getId())
                .thenReturn(100L);

        when(
                traceabilityLinkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                context.sourceSynthesis().getProject().getId(),
                                200L,
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                traceabilityLinkRepository.save(
                        any(TraceabilityLink.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        recorder.record(
                context.sourceSynthesis(),
                context.aiSynthesis(),
                context.audit()
        );

        verify(
                traceabilityLinkRepository
        ).save(
                argThat(link -> {

                    String rationale =
                            link.getRationale();

                    return rationale != null
                            && rationale.contains("mode=REFINE_THESIS")
                            && rationale.contains("validationStatus=VALID")
                            && rationale.contains("FND-001")
                            && rationale.contains("PRB-001")
                            && rationale.contains("OBJ-001")
                            && rationale.contains("OPP-001")
                            && rationale.contains("DO_NOT_INTRODUCE_NEW_FACTS");
                })
        );
    }

    @Test
    void shouldCreateDerivedFromLinkBetweenAiAndDeterministicSynthesis() {
        Context context =
                context();

        when(
                synthesisTraceabilityRegistrar.register(
                        context.aiSynthesis()
                )
        ).thenReturn(
                aiNode
        );

        when(
                synthesisTraceabilityRegistrar.register(
                        context.sourceSynthesis()
                )
        ).thenReturn(
                deterministicNode
        );

        when(aiNode.getId())
                .thenReturn(200L);

        when(deterministicNode.getId())
                .thenReturn(100L);

        when(
                traceabilityLinkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                context.sourceSynthesis().getProject().getId(),
                                200L,
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                traceabilityLinkRepository.save(
                        any(TraceabilityLink.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        recorder.record(
                context.sourceSynthesis(),
                context.aiSynthesis(),
                context.audit()
        );

        verify(
                synthesisTraceabilityRegistrar
        ).register(
                context.aiSynthesis()
        );

        verify(
                synthesisTraceabilityRegistrar
        ).register(
                context.sourceSynthesis()
        );

        verify(
                traceabilityLinkRepository
        ).save(
                argThat(link ->
                        link.getSourceNode() == aiNode
                                && link.getTargetNode() == deterministicNode
                                && link.getRelationType()
                                == TraceabilityRelationType.DERIVED_FROM
                                && link.getStrength()
                                == TraceabilityStrength.STRONG
                                && link.getOrigin()
                                == TraceabilityOrigin.AI_ASSISTED
                                && link.isRequiresReview()
                                && link.getRationale()
                                .contains(
                                        "validationStatus=VALID"
                                )
                )
        );
    }

    @Test
    void shouldBeIdempotentWhenDerivedLinkAlreadyExists() {
        Context context =
                context();

        TraceabilityLink existingLink =
                mock(
                        TraceabilityLink.class
                );

        when(
                synthesisTraceabilityRegistrar.register(
                        context.aiSynthesis()
                )
        ).thenReturn(
                aiNode
        );

        when(
                synthesisTraceabilityRegistrar.register(
                        context.sourceSynthesis()
                )
        ).thenReturn(
                deterministicNode
        );

        when(aiNode.getId())
                .thenReturn(200L);

        when(deterministicNode.getId())
                .thenReturn(100L);

        when(
                traceabilityLinkRepository
                        .findByProjectIdAndSourceNodeIdAndTargetNodeIdAndRelationType(
                                context.sourceSynthesis().getProject().getId(),
                                200L,
                                100L,
                                TraceabilityRelationType.DERIVED_FROM
                        )
        ).thenReturn(
                Optional.of(
                        existingLink
                )
        );

        recorder.record(
                context.sourceSynthesis(),
                context.aiSynthesis(),
                context.audit()
        );

        verify(
                traceabilityLinkRepository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void shouldRejectNonDeterministicSource() {
        Context context =
                context();

        StrategicSynthesis invalidSource =
                synthesis(
                        context.evidence(),
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        "Invalid source thesis"
                );

        assertThatThrownBy(() ->
                recorder.record(
                        invalidSource,
                        context.aiSynthesis(),
                        context.audit()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "DETERMINISTIC"
                );

        verifyNoInteractions(
                synthesisTraceabilityRegistrar,
                traceabilityLinkRepository
        );
    }

    @Test
    void shouldRejectNonAiDerivedSynthesis() {
        Context context =
                context();

        StrategicSynthesis invalidTarget =
                synthesis(
                        context.evidence(),
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Another deterministic thesis"
                );

        assertThatThrownBy(() ->
                recorder.record(
                        context.sourceSynthesis(),
                        invalidTarget,
                        context.audit()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "AI_ASSISTED"
                );

        verifyNoInteractions(
                synthesisTraceabilityRegistrar,
                traceabilityLinkRepository
        );
    }

    @Test
    void shouldRejectUnpersistedNodes() {
        Context context =
                context();

        when(
                synthesisTraceabilityRegistrar.register(
                        context.aiSynthesis()
                )
        ).thenReturn(
                aiNode
        );

        when(
                synthesisTraceabilityRegistrar.register(
                        context.sourceSynthesis()
                )
        ).thenReturn(
                deterministicNode
        );

        when(aiNode.getId())
                .thenReturn(null);

        when(deterministicNode.getId())
                .thenReturn(100L);

        assertThatThrownBy(() ->
                recorder.record(
                        context.sourceSynthesis(),
                        context.aiSynthesis(),
                        context.audit()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "persistidos"
                );

        verify(
                traceabilityLinkRepository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void shouldRejectNullAudit() {
        Context context =
                context();

        assertThatThrownBy(() ->
                recorder.record(
                        context.sourceSynthesis(),
                        context.aiSynthesis(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "audit"
                );

        verifyNoInteractions(
                synthesisTraceabilityRegistrar,
                traceabilityLinkRepository
        );
    }

    private static Context context() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis source =
                synthesis(
                        evidence,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        "Deterministic thesis"
                );

        StrategicSynthesis ai =
                synthesis(
                        evidence,
                        StrategicSynthesisOrigin.AI_ASSISTED,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        "AI refined thesis"
                );

        StrategicInterpretationRequest request =
                StrategicInterpretationRequest.from(
                        source,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        StrategicInterpretationResult result =
                StrategicInterpretationResult.of(
                        ai.getStrategicThesis(),
                        null,
                        source.getSourceArtifactCodes()
                );

        StrategicInterpretationValidationResult validation =
                StrategicInterpretationValidationResult.valid();

        StrategicInterpretationAudit audit =
                StrategicInterpretationAudit.from(
                        request,
                        result,
                        validation
                );

        return new Context(
                evidence,
                source,
                ai,
                audit
        );
    }

    private static StrategicSynthesis synthesis(
            SourceEvidence evidence,
            StrategicSynthesisOrigin origin,
            StrategicSynthesisStatus status,
            String thesis
    ) {
        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Business problem",
                "Business objective",
                "Strategic opportunity",
                thesis,
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of(
                                "EVD-001"
                        ),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                origin,
                status,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }

    private record Context(
            SourceEvidence evidence,
            StrategicSynthesis sourceSynthesis,
            StrategicSynthesis aiSynthesis,
            StrategicInterpretationAudit audit
    ) {
    }
}