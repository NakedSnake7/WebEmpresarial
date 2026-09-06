package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.ProvenanceRecorder;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityCodeGenerator;
import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
import com.webempresarial.store.digitaltransformation.domain.evidence.*;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterEvidenceIntoTraceabilityServiceTest {

    @Mock
    private SourceEvidenceRepository evidenceRepository;

    @Mock
    private TraceabilityNodeRepository nodeRepository;

    @Mock
    private EvidenceRegistrationPolicy registrationPolicy;

    @Mock
    private TraceabilityCodeGenerator codeGenerator;

    @Mock
    private TraceabilityNodeRegistrar nodeRegistrar;

    @Mock
    private ProvenanceRecorder provenanceRecorder;

    private RegisterEvidenceIntoTraceabilityService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        service =
                new RegisterEvidenceIntoTraceabilityService(
                        evidenceRepository,
                        nodeRepository,
                        registrationPolicy,
                        codeGenerator,
                        nodeRegistrar,
                        provenanceRecorder
                );
    }

    @Test
    void shouldRegisterVerifiedEvidenceIntoTraceability() {
        SourceEvidence evidence =
                verifiedEvidence();

        TraceabilityNode node =
                TraceabilityNode.create(
                        evidence.getProject(),
                        "NODE-EVD-AUDIT-001",
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        TraceabilityOrigin.MANUAL,
                        evidence.getStatement(),
                        "Descripción",
                        evidence.getEvidenceCode(),
                        "SourceEvidence",
                        false
                );

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(Optional.of(evidence));

        when(registrationPolicy.evaluate(evidence))
                .thenReturn(
                        EvidenceRegistrationDecision.approved(
                                "Aprobada"
                        )
                );

        when(
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                evidence.getProject().getId(),
                                TraceabilityNodeType.SOURCE_EVIDENCE,
                                evidence.getEvidenceCode()
                        )
        ).thenReturn(Optional.empty());

        when(
                codeGenerator.generateForExternalReference(
                        TraceabilityNodeType.SOURCE_EVIDENCE,
                        evidence.getEvidenceCode()
                )
        ).thenReturn("NODE-EVD-AUDIT-001");

        when(
                nodeRegistrar.register(
                        any(),
                        anyString(),
                        any(),
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyBoolean()
                )
        ).thenReturn(node);

        EvidenceTraceabilityRegistrationResult result =
                service.register(
                        1L,
                        50L,
                        "Jovani Amacende"
                );

        assertThat(result.registered()).isTrue();
        assertThat(result.existing()).isFalse();
        assertThat(result.traceabilityNodeCode())
                .isEqualTo("NODE-EVD-AUDIT-001");

        verify(provenanceRecorder)
                .recordNodeAction(
                        eq(evidence.getProject()),
                        eq(node),
                        eq(ProvenanceAction.GENERATED),
                        eq(TraceabilityOrigin.MANUAL),
                        eq("Jovani Amacende"),
                        eq("USER"),
                        anyString(),
                        contains("EVD-AUDIT-001")
                );
    }

    @Test
    void shouldSkipEvidenceRejectedByPolicy() {
        SourceEvidence evidence =
                verifiedEvidence();

        when(
                evidenceRepository.findByIdAndProjectStoreId(
                        50L,
                        1L
                )
        ).thenReturn(Optional.of(evidence));

        when(registrationPolicy.evaluate(evidence))
                .thenReturn(
                        EvidenceRegistrationDecision.rejected(
                                "No estratégica"
                        )
                );

        EvidenceTraceabilityRegistrationResult result =
                service.register(
                        1L,
                        50L,
                        "Jovani Amacende"
                );

        assertThat(result.registered()).isFalse();
        assertThat(result.decisionReason())
                .isEqualTo("No estratégica");

        verifyNoInteractions(
                codeGenerator,
                nodeRegistrar,
                provenanceRecorder
        );
    }

    private static SourceEvidence verifiedEvidence() {
        TransformationSourceDocument source =
                TestSources.validSource();

        SourceEvidence evidence =
                SourceEvidence.extract(
                        source.getProject(),
                        source,
                        null,
                        "EVD-AUDIT-001",
                        EvidenceClassification.STRATEGIC_FINDING,
                        EvidenceConfidence.EXPLICIT,
                        EvidenceExtractionOrigin.MANUAL,
                        "La marca es más fuerte que la plataforma digital.",
                        "La marca de Robert es hoy más fuerte que su plataforma digital.",
                        "Existe una brecha de percepción.",
                        EvidenceLocator.page(2)
                );

        evidence.verify("Jovani Amacende");

        return evidence;
    }
}