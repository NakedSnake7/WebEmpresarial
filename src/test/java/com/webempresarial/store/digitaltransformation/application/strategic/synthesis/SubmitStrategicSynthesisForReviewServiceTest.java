package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SubmitStrategicSynthesisForReviewServiceTest {
	
    @Mock
    private StrategicSynthesisTraceabilityRegistrar traceabilityRegistrar;

	@Mock
	private StrategicSynthesisGovernanceProvenanceRecorder provenanceRecorder;

	private SubmitStrategicSynthesisForReviewService service;

	@BeforeEach
	void setUp() {
	    MockitoAnnotations.openMocks(this);

	        service =
	                new SubmitStrategicSynthesisForReviewService(
	                        traceabilityRegistrar,
	                        provenanceRecorder
	                );
	    }

    @Test
    void shouldSubmitReadySynthesisForReview() {
        StrategicSynthesis original =
                synthesis(
                        StrategicSynthesisStatus.READY
                );

        SubmitStrategicSynthesisResult result =
                service.submit(original);

        assertThat(result.previousStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );

        assertThat(result.resultingStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(result.synthesis().getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.REQUIRES_REVIEW
                );

        assertThat(original.getStatus())
                .isEqualTo(
                        StrategicSynthesisStatus.READY
                );

        assertThat(result.synthesis())
                .isNotSameAs(original);
    }

    @Test
    void shouldRejectAlreadyApprovedSynthesis() {
        assertThatThrownBy(() ->
                service.submit(
                        synthesis(
                                StrategicSynthesisStatus.APPROVED
                        )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    private static StrategicSynthesis synthesis(
            StrategicSynthesisStatus status
    ) {
        SourceEvidence evidence =
                TestSources.validEvidence();

        return StrategicSynthesis.create(
                evidence.getProject(),
                "Finding",
                "Problem",
                "Objective",
                "Opportunity",
                "Strategic thesis",
                StrategicSynthesisEvidenceSummary.of(
                        StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                        100,
                        List.of("EVD-001"),
                        4
                ),
                StrategicSynthesisConfidence.HIGH,
                StrategicSynthesisOrigin.DETERMINISTIC,
                status,
                List.of(
                        "FND-001",
                        "PRB-001",
                        "OBJ-001",
                        "OPP-001"
                )
        );
    }
}