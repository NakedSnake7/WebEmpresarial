package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StrategicSynthesisGovernanceProvenanceRecorderTest {

    @Mock
    private ProvenanceRecorder provenanceRecorder;

    private StrategicSynthesisGovernanceProvenanceRecorder recorder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recorder =
                new StrategicSynthesisGovernanceProvenanceRecorder(
                        provenanceRecorder
                );
    }

    @Test
    void shouldRecordSubmissionAgainstSynthesisNode() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        recorder.recordSubmission(
                context.synthesis(),
                context.node(),
                StrategicSynthesisStatus.READY,
                StrategicSynthesisStatus.REQUIRES_REVIEW
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(context.synthesis().getProject()),
                        same(context.node()),
                        eq(ProvenanceAction.SUBMITTED_FOR_REVIEW),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq("StrategicSynthesisGovernance"),
                        eq("SYSTEM"),
                        eq("StrategicSynthesisGovernance"),
                        argThat(explanation ->
                                explanation.contains(
                                        "previousStatus=READY"
                                )
                                        && explanation.contains(
                                                "resultingStatus=REQUIRES_REVIEW"
                                        )
                                        && explanation.contains(
                                                "origin=DETERMINISTIC"
                                        )
                                        && explanation.contains(
                                                "FND-001"
                                        )
                                        && explanation.contains(
                                                "OPP-001"
                                        )
                        )
                );

        verifyNoMoreInteractions(
                provenanceRecorder
        );
    }

    @Test
    void shouldRecordHumanApprovalAgainstSynthesisNode() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        context.synthesis(),
                        "consultant@example.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis fue validada.",
                        Instant.parse(
                                "2026-08-10T18:00:00Z"
                        )
                );

        recorder.recordReview(
                review,
                context.node()
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(context.synthesis().getProject()),
                        same(context.node()),
                        eq(ProvenanceAction.VERIFIED),
                        eq(TraceabilityOrigin.MANUAL),
                        eq("consultant@example.com"),
                        eq("HUMAN_CONSULTANT"),
                        eq("StrategicSynthesisGovernance"),
                        argThat(explanation ->
                                explanation.contains(
                                        "decision=APPROVE"
                                )
                                        && explanation.contains(
                                                "resultingStatus=APPROVED"
                                        )
                                        && explanation.contains(
                                                "La síntesis fue validada."
                                        )
                        )
                );
    }

    @Test
    void shouldRecordProjectOwnerRejection() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        context.synthesis(),
                        "owner@example.com",
                        StrategicSynthesisReviewerType.PROJECT_OWNER,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La tesis requiere ajustes.",
                        Instant.parse(
                                "2026-08-10T18:05:00Z"
                        )
                );

        recorder.recordReview(
                review,
                context.node()
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(context.synthesis().getProject()),
                        same(context.node()),
                        eq(ProvenanceAction.REJECTED),
                        eq(TraceabilityOrigin.MANUAL),
                        eq("owner@example.com"),
                        eq("PROJECT_OWNER"),
                        eq("StrategicSynthesisGovernance"),
                        contains(
                                "decision=REJECT"
                        )
                );
    }

    @Test
    void shouldRecordSystemReviewAsSystemGenerated() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        context.synthesis(),
                        "system",
                        StrategicSynthesisReviewerType.SYSTEM,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "Deterministic automatic approval.",
                        Instant.parse(
                                "2026-08-10T18:10:00Z"
                        )
                );

        recorder.recordReview(
                review,
                context.node()
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(context.synthesis().getProject()),
                        same(context.node()),
                        eq(ProvenanceAction.VERIFIED),
                        eq(TraceabilityOrigin.SYSTEM_GENERATED),
                        eq("system"),
                        eq("SYSTEM"),
                        eq("StrategicSynthesisGovernance"),
                        contains(
                                "decision=APPROVE"
                        )
                );
    }

    @Test
    void shouldPreserveAiOriginInExplanation() {
        Context context =
                context(
                        StrategicSynthesisOrigin.AI_ASSISTED
                );

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        context.synthesis(),
                        "consultant@example.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "AI synthesis manually validated.",
                        Instant.parse(
                                "2026-08-10T18:15:00Z"
                        )
                );

        recorder.recordReview(
                review,
                context.node()
        );

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(context.synthesis().getProject()),
                        same(context.node()),
                        eq(ProvenanceAction.VERIFIED),
                        eq(TraceabilityOrigin.MANUAL),
                        eq("consultant@example.com"),
                        eq("HUMAN_CONSULTANT"),
                        eq("StrategicSynthesisGovernance"),
                        argThat(explanation ->
                                explanation.contains(
                                        "synthesisOrigin=AI_ASSISTED"
                                )
                                        && explanation.contains(
                                                "AI synthesis manually validated."
                                        )
                        )
                );
    }

    @Test
    void shouldRejectNodeOfWrongType() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        TraceabilityNode wrongNode =
                TraceabilityNode.create(
                        context.synthesis().getProject(),
                        "OPP-999",
                        TraceabilityNodeType.STRATEGIC_OPPORTUNITY,
                        TraceabilityOrigin.SYSTEM_GENERATED,
                        "Opportunity",
                        null,
                        "OPP-999",
                        "StrategicArtifact",
                        false
                );

        assertThatThrownBy(() ->
                recorder.recordSubmission(
                        context.synthesis(),
                        wrongNode,
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "STRATEGIC_SYNTHESIS"
                );

        verifyNoInteractions(
                provenanceRecorder
        );
    }

    @Test
    void shouldRejectNullSynthesisNode() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThatThrownBy(() ->
                recorder.recordSubmission(
                        context.synthesis(),
                        null,
                        StrategicSynthesisStatus.READY,
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "nodo"
                );

        verifyNoInteractions(
                provenanceRecorder
        );
    }

    @Test
    void shouldRejectNullReview() {
        Context context =
                context(
                        StrategicSynthesisOrigin.DETERMINISTIC
                );

        assertThatThrownBy(() ->
                recorder.recordReview(
                        null,
                        context.node()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "revisión"
                );

        verifyNoInteractions(
                provenanceRecorder
        );
    }

    private static Context context(
            StrategicSynthesisOrigin origin
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis synthesis =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Business problem",
                        "Business objective",
                        "Strategic opportunity",
                        "Strategic thesis",
                        StrategicSynthesisEvidenceSummary.of(
                                StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                                100,
                                List.of(
                                        "EVD-AUDIT-001"
                                ),
                                4
                        ),
                        origin == StrategicSynthesisOrigin.AI_ASSISTED
                                ? StrategicSynthesisConfidence.MEDIUM
                                : StrategicSynthesisConfidence.HIGH,
                        origin,
                        StrategicSynthesisStatus.REQUIRES_REVIEW,
                        List.of(
                                "FND-001",
                                "PRB-001",
                                "OBJ-001",
                                "OPP-001"
                        )
                );

        TraceabilityNode node =
                TraceabilityNode.create(
                        synthesis.getProject(),
                        "SYN-001",
                        TraceabilityNodeType.STRATEGIC_SYNTHESIS,
                        origin == StrategicSynthesisOrigin.HUMAN_AUTHORED
                                ? TraceabilityOrigin.MANUAL
                                : TraceabilityOrigin.SYSTEM_GENERATED,
                        synthesis.getStrategicThesis(),
                        "Strategic synthesis traceability node",
                        "SYNTHESIS-001",
                        "StrategicSynthesis",
                        true
                );

        return new Context(
                synthesis,
                node
        );
    }

    private record Context(
            StrategicSynthesis synthesis,
            TraceabilityNode node
    ) {
    }
}